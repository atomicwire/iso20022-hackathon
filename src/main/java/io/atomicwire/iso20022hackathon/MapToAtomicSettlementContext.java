package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.iso20022.bridge.ForeignExchangeTradeBridge;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.time.Instant;
import java.util.UUID;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * Map an incoming settlement request to a context containing a conceptual representation of the
 * trade and a UID for correlation.
 */
public class MapToAtomicSettlementContext
    implements MapFunction<ForeignExchangeTradeInstructionV04, AtomicSettlementContext> {

  @Override
  public AtomicSettlementContext map(ForeignExchangeTradeInstructionV04 message) {
    UUID internalUid = UUID.randomUUID();
    ForeignExchangeTrade foreignExchangeTrade = ForeignExchangeTradeBridge.INSTANCE.bridge(message);
    Instant now = Instant.now();
    return new AtomicSettlementContext(internalUid, foreignExchangeTrade, message, now);
  }
}
