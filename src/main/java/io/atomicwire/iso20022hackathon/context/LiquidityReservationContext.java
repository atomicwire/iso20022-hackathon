package io.atomicwire.iso20022hackathon.context;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.UUID;
import lombok.Data;
import lombok.NonNull;

@Data
public class LiquidityReservationContext {

  @NonNull private final UUID internalUid;

  @NonNull private final PaymentObligation paymentObligation;

  private final boolean success = true;
}
