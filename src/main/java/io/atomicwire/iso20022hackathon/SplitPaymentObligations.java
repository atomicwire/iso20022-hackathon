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
