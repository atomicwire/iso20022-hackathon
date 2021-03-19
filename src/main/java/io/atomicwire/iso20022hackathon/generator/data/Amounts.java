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

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Amounts {

  private static final int FIFTY_THOUSAND = 50_000;

  /** Choose a random amount, between 100K and 10M inclusive, rounded down to the nearest 50K. */
  public static BigDecimal chooseAmount() {
    Random random = ThreadLocalRandom.current();
    int fiftyThousands =
        (100_000 / FIFTY_THOUSAND)
            + random.nextInt(10_000_000 / FIFTY_THOUSAND - (100_000 / FIFTY_THOUSAND) + 1);
    return new BigDecimal(FIFTY_THOUSAND * fiftyThousands);
  }
}
