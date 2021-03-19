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

package io.atomicwire.iso20022hackathon.generator;

import io.atomicwire.iso20022hackathon.generator.data.Amounts;
import io.atomicwire.iso20022hackathon.generator.data.Bics;
import io.atomicwire.iso20022hackathon.generator.data.Bics.BicPair;
import io.atomicwire.iso20022hackathon.generator.data.Currencies;
import io.atomicwire.iso20022hackathon.generator.data.Currencies.CurrencyPair;
import io.atomicwire.iso20022hackathon.generator.data.Currency;
import io.atomicwire.iso20022hackathon.generator.data.ExchangeRates;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.streaming.api.functions.source.datagen.DataGenerator;

public class ForeignExchangeTradeGenerator
    implements DataGenerator<ForeignExchangeTradeInstructionV04> {

  @Override
  public void open(
      String name, FunctionInitializationContext context, RuntimeContext runtimeContext)
      throws Exception {
    // No state to keep
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public ForeignExchangeTradeInstructionV04 next() {
    return generateTrade();
  }

  private static ForeignExchangeTradeInstructionV04 generateTrade() {
    CurrencyPair currencyPair = Currencies.chooseRandomCurrencyPair();
    Currency tradingSideBuyAmountCurrency = currencyPair.getCurrency1();
    Currency tradingSideSellAmountCurrency = currencyPair.getCurrency2();

    // Apply a fuzzy exchange rate, then round down to 10K
    BigDecimal tradingSideBuyAmount = Amounts.chooseAmount();
    double exchangeRate =
        ExchangeRates.getFuzzedExchangeRate(
            tradingSideBuyAmountCurrency, tradingSideSellAmountCurrency);
    BigDecimal tradingSideSellAmount =
        tradingSideBuyAmount
            .multiply(new BigDecimal(exchangeRate))
            .setScale(-4, RoundingMode.FLOOR);

    BicPair tradingSideBuyAmountCurrencyBicPair =
        Bics.chooseRandomBicPairByCurrency(tradingSideBuyAmountCurrency);
    BicPair tradingSideSellAmountCurrencyBicPair =
        Bics.chooseRandomBicPairByCurrency(tradingSideSellAmountCurrency);

    String tradingSideDeliveryAgentBic = tradingSideSellAmountCurrencyBicPair.getBic1();
    String tradingSideReceivingAgentBic = tradingSideBuyAmountCurrencyBicPair.getBic1();
    String counterpartySideDeliveryAgentBic = tradingSideBuyAmountCurrencyBicPair.getBic2();
    String counterpartySideReceivingAgentBic = tradingSideSellAmountCurrencyBicPair.getBic2();

    return new ForeignExchangeTradeInstructionV04(
        tradingSideBuyAmount,
        tradingSideBuyAmountCurrency.name(),
        tradingSideSellAmount,
        tradingSideSellAmountCurrency.name(),
        tradingSideDeliveryAgentBic,
        tradingSideReceivingAgentBic,
        counterpartySideDeliveryAgentBic,
        counterpartySideReceivingAgentBic);
  }
}
