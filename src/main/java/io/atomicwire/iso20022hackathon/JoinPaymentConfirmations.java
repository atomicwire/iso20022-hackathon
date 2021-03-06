/*
* Copyright 2021 Atomic Wire Technology Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.context.PaymentContext;
import java.util.stream.StreamSupport;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * This class joins two input streams, one of {@link AtomicSettlementContext}s representing each
 * transaction, and one of {@link PaymentContext}s which represent the results of the payments
 * necessary to fulfill the settlement. It collects and stores the expected objects until everything
 * has arrived, then forwards the {@link AtomicSettlementContext} downstream.
 */
public class JoinPaymentConfirmations
    extends RichCoFlatMapFunction<
        AtomicSettlementContext, PaymentContext, AtomicSettlementContext> {

  private transient ValueState<AtomicSettlementContext> settlementContextState;
  private transient ListState<PaymentContext> paymentConfirmationContextsState;

  @Override
  public void open(Configuration parameters) {
    ValueStateDescriptor<AtomicSettlementContext> settlementContextStateDescriptor =
        new ValueStateDescriptor<>("settlementContext", AtomicSettlementContext.class);
    settlementContextState = getRuntimeContext().getState(settlementContextStateDescriptor);

    ListStateDescriptor<PaymentContext> paymentConfirmationContextsStateDescriptor =
        new ListStateDescriptor<>("paymentConfirmationContexts", PaymentContext.class);
    paymentConfirmationContextsState =
        getRuntimeContext().getListState(paymentConfirmationContextsStateDescriptor);
  }

  @Override
  public void flatMap1(AtomicSettlementContext value, Collector<AtomicSettlementContext> out)
      throws Exception {
    settlementContextState.update(value);
    emitIfAllLiquidityReserved(out);
  }

  @Override
  public void flatMap2(PaymentContext value, Collector<AtomicSettlementContext> out)
      throws Exception {
    paymentConfirmationContextsState.add(value);
    emitIfAllLiquidityReserved(out);
  }

  /**
   * If the {@link AtomicSettlementContext} for this transaction along with all necessary {@link
   * PaymentContext}s have been collected, emit the settlement context and clear state.
   */
  private void emitIfAllLiquidityReserved(Collector<AtomicSettlementContext> out) throws Exception {
    AtomicSettlementContext settlementContext = settlementContextState.value();
    if (settlementContext == null) {
      return; // Still awaiting the settlement context
    }

    int expectedPaymentConfirmations =
        settlementContext.getForeignExchangeTrade().getResultingSettlements().size();
    long receivedPaymentConfirmations =
        StreamSupport.stream(paymentConfirmationContextsState.get().spliterator(), false).count();

    if (receivedPaymentConfirmations < expectedPaymentConfirmations) {
      return; // Still awaiting at least one payment confirmation
    }

    // The settlement context and all necessary payment confirmations have arrived; emit the context
    // downstream and clear state
    out.collect(settlementContext);
    settlementContextState.clear();
    paymentConfirmationContextsState.clear();
  }
}
