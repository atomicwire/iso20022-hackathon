package io.atomicwire.iso20022hackathon.iso20022.conceptual;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NonNull;

@Data
public class PaymentObligation {

  @NonNull private final BigDecimal amount;

  @NonNull private final String currency;

  @NonNull private final String deliveryAgentBic;

  @NonNull private final String receivingAgentBic;
}
