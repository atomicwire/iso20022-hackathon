package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentObligationContext {

  private final UUID internalUid;

  private final PaymentObligation paymentObligation;
}
