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

package io.atomicwire.iso20022hackathon.generator.data;

import static io.atomicwire.iso20022hackathon.generator.data.Currencies.chooseRandomCurrencyPair;
import static org.assertj.core.api.Assertions.assertThat;

import io.atomicwire.iso20022hackathon.generator.data.Currencies.CurrencyPair;
import org.junit.jupiter.api.Test;

public class CurrenciesTests {

  @Test
  void testChooseRandomCurrencyPair() {
    for (int i = 0; i < 1000; i++) {
      CurrencyPair pair = chooseRandomCurrencyPair();
      assertThat(pair.getCurrency1()).isNotEqualTo(pair.getCurrency2());
    }
  }
}
