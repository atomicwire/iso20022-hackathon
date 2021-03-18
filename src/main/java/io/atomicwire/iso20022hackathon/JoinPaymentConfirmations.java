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
