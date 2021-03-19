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

package io.atomicwire.iso20022hackathon.context;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.ForeignExchangeTrade;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.time.Instant;
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

  /** The timestamp when this context was created. */
  @NonNull private final Instant startTimestamp;
}
