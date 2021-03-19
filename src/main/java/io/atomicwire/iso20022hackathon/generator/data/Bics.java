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

import io.atomicwire.iso20022hackathon.annotation.VisibleForTesting;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.NonNull;
import lombok.Value;

public class Bics {

  private static final List<String> EUR_BICS =
      list(
          "AAAABE10XXX",
          "BBBBDE10XXX",
          "CCCCFR10XXX",
          "DDDDIT10XXX",
          "EEEENL10XXX",
          "FFFFBE10XXX",
          "GGGGDE10XXX",
          "HHHHFR10XXX",
          "IIIIIT10XXX",
          "JJJJNL10XXX");

  private static final List<String> USD_BICS =
      list(
          "AAAAUS10XXX",
          "BBBBUS10XXX",
          "CCCCUS10XXX",
          "DDDDUS10XXX",
          "EEEEUS10XXX",
          "FFFFUS10XXX",
          "GGGGUS10XXX",
          "HHHHUS10XXX",
          "IIIIUS10XXX",
          "JJJJUS10XXX");

  private static final List<String> SGD_BICS =
      list(
          "AAAASG10XXX",
          "BBBBSG10XXX",
          "CCCCSG10XXX",
          "DDDDSG10XXX",
          "EEEESG10XXX",
          "FFFFSG10XXX",
          "GGGGSG10XXX",
          "HHHHSG10XXX",
          "IIIISG10XXX",
          "JJJJSG10XXX");

  public static BicPair chooseRandomBicPairByCurrency(Currency currency) {
    switch (requireNonNull(currency, "currency")) {
      case EUR:
        return chooseRandomBicPair(EUR_BICS);
      case USD:
        return chooseRandomBicPair(USD_BICS);
      case SGD:
        return chooseRandomBicPair(SGD_BICS);
      default:
        throw new UnsupportedOperationException("Unsupported currency: " + currency);
    }
  }

  @VisibleForTesting
  static BicPair chooseRandomBicPair(List<String> list) {
    Random random = ThreadLocalRandom.current();
    int idx1 = random.nextInt(list.size());
    int idx2 = random.nextInt(list.size());

    final String bic1 = list.get(idx1);
    final String bic2;

    if (idx2 != idx1) {
      bic2 = list.get(idx2);
    } else {
      bic2 = list.get((idx2 + 1) % list.size());
    }

    return new BicPair(bic1, bic2);
  }

  @SafeVarargs
  private static <T> List<T> list(T... items) {
    return Collections.unmodifiableList(Arrays.asList(items));
  }

  @Value
  public static class BicPair {
    @NonNull String bic1;
    @NonNull String bic2;
  }
}
