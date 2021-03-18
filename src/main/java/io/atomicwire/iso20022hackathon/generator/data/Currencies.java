package io.atomicwire.iso20022hackathon.generator.data;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.NonNull;
import lombok.Value;

public class Currencies {

  private static final Random RANDOM = ThreadLocalRandom.current();

  public static CurrencyPair chooseRandomCurrencyPair() {
    Currency[] currencies = Currency.values();
    int idx1 = RANDOM.nextInt(currencies.length);
    int idx2 = RANDOM.nextInt(currencies.length);

    final Currency currency1 = currencies[idx1];
    final Currency currency2;

    if (idx2 != idx1) {
      currency2 = currencies[idx2];
    } else {
      currency2 = currencies[(idx2 + 1) % currencies.length];
    }

    return new CurrencyPair(currency1, currency2);
  }

  @Value
  public static class CurrencyPair {
    @NonNull Currency currency1;
    @NonNull Currency currency2;
  }
}