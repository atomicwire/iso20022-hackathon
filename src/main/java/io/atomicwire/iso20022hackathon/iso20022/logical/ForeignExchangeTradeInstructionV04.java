package io.atomicwire.iso20022hackathon.iso20022.logical;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NonNull;

/** A mock representation of a logical ISO 20022 fxtr.014.001.004 message. */
@Data
public class ForeignExchangeTradeInstructionV04 {

  @NonNull private final BigDecimal tradingSideBuyAmount;

  @NonNull private final String tradingSideBuyAmountCurrency;

  @NonNull private final BigDecimal tradingSideSellAmount;

  @NonNull private final String tradingSideSellAmountCurrency;

  @NonNull private final String tradingSideDeliveryAgentBic;

  @NonNull private final String tradingSideReceivingAgentBic;

  @NonNull private final String counterpartySideDeliveryAgentBic;

  @NonNull private final String counterpartySideReceivingAgentBic;
}
