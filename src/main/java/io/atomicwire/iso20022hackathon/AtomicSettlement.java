package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.generator.ForeignExchangeTradeGenerator;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;

public class AtomicSettlement {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.addSource(new DataGeneratorSource<>(new ForeignExchangeTradeGenerator(), 1, 10L))
        .returns(ForeignExchangeTradeInstructionV04.class)
        .map(AtomicSettlement::formatTrade)
        .print();

    env.execute(AtomicSettlement.class.getSimpleName());
  }

  private static String formatTrade(ForeignExchangeTradeInstructionV04 trade) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "Received trade to buy %s %.2f and sell %s %.2f%n",
            trade.getTradingSideBuyAmountCurrency(),
            trade.getTradingSideBuyAmount(),
            trade.getTradingSideSellAmountCurrency(),
            trade.getTradingSideSellAmount()));
    sb.append(
        String.format(
            "- Trading side buy: %s -> %s%n",
            trade.getCounterpartySideDeliveryAgentBic(), trade.getTradingSideReceivingAgentBic()));
    sb.append(
        String.format(
            "- Trading side sell: %s -> %s%n",
            trade.getTradingSideDeliveryAgentBic(), trade.getCounterpartySideReceivingAgentBic()));
    return sb.toString();
  }
}
