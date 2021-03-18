package io.atomicwire.iso20022hackathon.generator.data;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Amounts {

  private static final int FIFTY_THOUSAND = 50_000;
  private static final Random RANDOM = ThreadLocalRandom.current();

  /** Choose a random amount, between 100K and 10M inclusive, rounded down to the nearest 50K. */
  public static BigDecimal chooseAmount() {
    int fiftyThousands =
        (100_000 / FIFTY_THOUSAND)
            + RANDOM.nextInt(10_000_000 / FIFTY_THOUSAND - (100_000 / FIFTY_THOUSAND) + 1);
    return new BigDecimal(FIFTY_THOUSAND * fiftyThousands);
  }
}
