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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ThreadLocalRandom;

public class ExchangeRates {

  private static final double EUR_TO_SGD = 1.6017742;
  private static final double SGD_TO_EUR = 1 / EUR_TO_SGD;
  private static final double EUR_TO_USD = 1.191878;
  private static final double USD_TO_EUR = 1 / EUR_TO_USD;
  private static final double SGD_TO_USD = 0.74409863;
  private static final double USD_TO_SGD = 1 / SGD_TO_USD;

  public static double getFuzzedExchangeRate(Currency from, Currency to) {
    requireNonNull(from, "from");
    requireNonNull(to, "to");

    switch (from) {
      case EUR:
        switch (to) {
          case SGD:
            return fuzz(EUR_TO_SGD);
          case USD:
            return fuzz(EUR_TO_USD);
        }
        break;
      case SGD:
        switch (to) {
          case EUR:
            return fuzz(SGD_TO_EUR);
          case USD:
            return fuzz(SGD_TO_USD);
        }
        break;
      case USD:
        switch (to) {
          case EUR:
            return fuzz(USD_TO_EUR);
          case SGD:
            return fuzz(USD_TO_SGD);
        }
        break;
    }

    throw new UnsupportedOperationException("Unsupported currency pair: " + from + "-" + to);
  }

  // Apply a ~1% "fuzz factor" to a rate
  private static double fuzz(double rate) {
    double fuzzFactor = 0.01 * rate * ThreadLocalRandom.current().nextGaussian();
    return rate + fuzzFactor;
  }
}
