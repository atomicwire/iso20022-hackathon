/*
* Copyright 2021 Atomic Wire Technology Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
