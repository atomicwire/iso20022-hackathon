package io.atomicwire.iso20022hackathon.iso20022.conceptual;

import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class ForeignExchangeTrade {

  @NonNull private final List<PaymentObligation> resultingSettlements;
}
