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
