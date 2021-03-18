package io.atomicwire.iso20022hackathon.context;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.util.UUID;
import lombok.Data;
import lombok.NonNull;

@Data
public class AtomicSettlementContext {

  /**
   * A unique ID used for correlation of events related to this specific trade within the atomic
   * settlement system.
   */
  @NonNull private final UUID internalUid;

  /** The ISO 20022 conceptual entity which is being settled. */
  @NonNull private final ForeignExchangeTrade foreignExchangeTrade;

  /** The original ISO 20022 logical message that initiated this settlement process. */
  @NonNull private final ForeignExchangeTradeInstructionV04 originalMessage;
}
