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

package io.atomicwire.iso20022hackathon.iso20022.logical;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** A mock representation of a logical ISO 20022 fxtr.014.001.004 message. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignExchangeTradeInstructionV04 {

  @NonNull private BigDecimal tradingSideBuyAmount;

  @NonNull private String tradingSideBuyAmountCurrency;

  @NonNull private BigDecimal tradingSideSellAmount;

  @NonNull private String tradingSideSellAmountCurrency;

  @NonNull private String tradingSideDeliveryAgentBic;

  @NonNull private String tradingSideReceivingAgentBic;

  @NonNull private String counterpartySideDeliveryAgentBic;

  @NonNull private String counterpartySideReceivingAgentBic;
}
