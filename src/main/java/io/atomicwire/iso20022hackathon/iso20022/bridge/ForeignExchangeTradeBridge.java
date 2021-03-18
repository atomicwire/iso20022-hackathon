package io.atomicwire.iso20022hackathon.iso20022.bridge;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.util.ArrayList;
import java.util.List;

/**
 * A bridge between a logical {@link ForeignExchangeTradeInstructionV04} message and conceptual
 * {@link ForeignExchangeTrade}.
 */
public class ForeignExchangeTradeBridge
    implements Bridge<ForeignExchangeTradeInstructionV04, ForeignExchangeTrade> {

  public static final ForeignExchangeTradeBridge INSTANCE = new ForeignExchangeTradeBridge();

  @Override
  public ForeignExchangeTrade bridge(
      ForeignExchangeTradeInstructionV04 foreignExchangeTradeInstructionV04) {
    PaymentObligation tradingSidePaymentObligation =
        new PaymentObligation(
            foreignExchangeTradeInstructionV04.getTradingSideSellAmount(),
            foreignExchangeTradeInstructionV04.getTradingSideSellAmountCurrency(),
            foreignExchangeTradeInstructionV04.getTradingSideDeliveryAgentBic(),
            foreignExchangeTradeInstructionV04.getCounterpartySideReceivingAgentBic());

    PaymentObligation counterpartySidePaymentObligation =
        new PaymentObligation(
            foreignExchangeTradeInstructionV04.getTradingSideBuyAmount(),
            foreignExchangeTradeInstructionV04.getTradingSideBuyAmountCurrency(),
            foreignExchangeTradeInstructionV04.getCounterpartySideDeliveryAgentBic(),
            foreignExchangeTradeInstructionV04.getTradingSideReceivingAgentBic());

    List<PaymentObligation> resultingSettlements = new ArrayList<>();
    resultingSettlements.add(tradingSidePaymentObligation);
    resultingSettlements.add(counterpartySidePaymentObligation);

    return new ForeignExchangeTrade(resultingSettlements);
  }
}
