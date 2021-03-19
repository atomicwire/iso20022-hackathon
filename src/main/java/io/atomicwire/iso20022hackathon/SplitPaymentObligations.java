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
import io.atomicwire.iso20022hackathon.context.PaymentObligationContext;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.UUID;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * Split the {@link PaymentObligation}s from a {@link ForeignExchangeTrade} and emit a {@link
 * PaymentObligationContext}, joining each {@link PaymentObligation} with the internal UID of the
 * transaction for later joining.
 */
public class SplitPaymentObligations
    implements FlatMapFunction<AtomicSettlementContext, PaymentObligationContext> {

  @Override
  public void flatMap(AtomicSettlementContext value, Collector<PaymentObligationContext> out) {
    UUID internalUid = value.getInternalUid();
    ForeignExchangeTrade foreignExchangeTrade = value.getForeignExchangeTrade();

    for (PaymentObligation paymentObligation : foreignExchangeTrade.getResultingSettlements()) {
      PaymentObligationContext paymentObligationContext =
          new PaymentObligationContext(internalUid, paymentObligation);
      out.collect(paymentObligationContext);
    }
  }
}
