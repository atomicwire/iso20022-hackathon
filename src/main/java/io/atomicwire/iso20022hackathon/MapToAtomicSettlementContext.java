package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.iso20022.bridge.ForeignExchangeTradeBridge;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.util.UUID;
import org.apache.flink.api.common.functions.MapFunction;

public class MapToAtomicSettlementContext
    implements MapFunction<ForeignExchangeTradeInstructionV04, AtomicSettlementContext> {

  @Override
  public AtomicSettlementContext map(ForeignExchangeTradeInstructionV04 message) {
    UUID internalUid = UUID.randomUUID();
    ForeignExchangeTrade foreignExchangeTrade = ForeignExchangeTradeBridge.INSTANCE.bridge(message);
    return new AtomicSettlementContext(internalUid, foreignExchangeTrade, message);
  }
}
