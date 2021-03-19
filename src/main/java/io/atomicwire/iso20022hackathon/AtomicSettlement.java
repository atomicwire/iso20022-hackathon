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

package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.context.LiquidityReservationContext;
import io.atomicwire.iso20022hackathon.context.PaymentContext;
import io.atomicwire.iso20022hackathon.context.PaymentObligationContext;
import io.atomicwire.iso20022hackathon.generator.ForeignExchangeTradeGenerator;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

@Slf4j
public class AtomicSettlement {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // When running with high parallelism, you may need to replace the previous line with the
    // following:
    /*
    Configuration configuration = new Configuration();
    configuration.set(TaskManagerOptions.NETWORK_MEMORY_MAX, MemorySize.ofMebiBytes(2 * 1024));
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
     */

    ParameterTool params = ParameterTool.fromArgs(args);
    boolean trace = params.getBoolean("trace", false);
    long rate = params.getInt("rate", 1);
    int parallelism = params.getInt("parallelism", 1);
    boolean printThroughput = params.getBoolean("print-throughput", false);
    boolean printLatency = params.getBoolean("print-latency", false);
    int outputFreqSecs = params.getInt("output-freq", 10);

    if (rate <= 0) {
      rate = Long.MAX_VALUE;
    }

    if (parallelism != 0) {
      env.setParallelism(parallelism);
    }

    log.info("trace: {}", trace ? "enabled" : "disabled");
    log.info("rate: {}", rate < Long.MAX_VALUE ? rate + " settlement requests/sec" : "unlimited");
    log.info("parallelism: {}", env.getParallelism());
    log.info("print-throughput: {}", printThroughput);
    log.info("print-latency: {}", printLatency);
    log.info("output-freq: every {} sec", outputFreqSecs);
    log.info("---");

    // Generate a stream of simulated foreign exchange trade settlement requests
    DataStream<ForeignExchangeTradeInstructionV04> settlementRequests =
        env.addSource(new DataGeneratorSource<>(new ForeignExchangeTradeGenerator(), rate, null))
            .returns(ForeignExchangeTradeInstructionV04.class);

    if (trace) {
      settlementRequests
          .map(
              request -> {
                log.info(
                    "<-- Received fxtr.014, Foreign Exchange Trade Instruction: buy {} {} for {} {}",
                    request.getTradingSideBuyAmountCurrency(),
                    request.getTradingSideBuyAmount().longValue(),
                    request.getTradingSideSellAmountCurrency(),
                    request.getTradingSideSellAmount().longValue());
                return null;
              })
          .addSink(new DiscardingSink<>());
    }

    // Map each settlement request to an AtomicSettlementContext, assigning a UID and bridging the
    // logical ISO 20022 input message to its conceptual representation, in this case a
    // ForeignExchangeTrade
    DataStream<AtomicSettlementContext> settlementContexts =
        settlementRequests.map(new MapToAtomicSettlementContext());

    // Split each settlement request into its constituent payment obligations
    DataStream<PaymentObligationContext> paymentObligationContexts =
        settlementContexts.flatMap(new SplitPaymentObligations());

    // Key the stream of payment obligations by (currency, delivery agent), so that a single
    // liquidity reservation task processes requests grouped on that basis
    KeyedStream<PaymentObligationContext, Tuple2<String, String>> keyedPaymentObligationContexts =
        paymentObligationContexts.keyBy(
            new KeySelector<PaymentObligationContext, Tuple2<String, String>>() {
              @Override
              public Tuple2<String, String> getKey(PaymentObligationContext value) {
                PaymentObligation paymentObligation = value.getPaymentObligation();

                String currency = paymentObligation.getCurrency();
                String deliveryAgentBic = paymentObligation.getDeliveryAgentBic();

                return Tuple2.of(currency, deliveryAgentBic);
              }
            });

    // Issue liquidity reservations asynchronously and collect the results
    DataStream<LiquidityReservationContext> liquidityReservationContexts =
        AsyncDataStream.unorderedWait(
            keyedPaymentObligationContexts,
            new ReserveLiquidity().withTrace(trace),
            1,
            TimeUnit.MINUTES,
            100_000);

    // Key the stream of liquidity reservations by the internal UID so they can be re-joined with
    // the original settlement request
    KeyedStream<LiquidityReservationContext, UUID> keyedLiquidityReservationContexts =
        liquidityReservationContexts.keyBy(LiquidityReservationContext::getInternalUid);

    // Join the stream of liquidity reservations with the original settlement context by internal
    // UID
    KeyedStream<AtomicSettlementContext, UUID> keyedSettlementContexts =
        settlementContexts.keyBy(AtomicSettlementContext::getInternalUid);
    DataStream<AtomicSettlementContext> readySettlementContexts =
        keyedSettlementContexts
            .connect(keyedLiquidityReservationContexts)
            .flatMap(new JoinLiquidityReservations());

    // Split each ready settlement request into its constituent payment obligations
    DataStream<PaymentObligationContext> readyPaymentObligationContexts =
        readySettlementContexts.flatMap(new SplitPaymentObligations());

    // Key the stream of payment obligations by internal UID to randomly distribute the settlements
    KeyedStream<PaymentObligationContext, UUID> keyedReadyPaymentObligationContexts =
        readyPaymentObligationContexts.keyBy(PaymentObligationContext::getInternalUid);

    // Issue settlements asynchronously and collect the results
    DataStream<PaymentContext> paymentConfirmationContexts =
        AsyncDataStream.unorderedWait(
            keyedReadyPaymentObligationContexts,
            new SettlePaymentObligation().withTrace(trace),
            1,
            TimeUnit.MINUTES,
            100_000);

    // Key the stream of payment confirmations by the internal UID so they can be re-joined with the
    // original settlement request
    KeyedStream<PaymentContext, UUID> keyedPaymentConfirmationContexts =
        paymentConfirmationContexts.keyBy(PaymentContext::getInternalUid);

    // Join the stream of payment confirmations with the original settlement context by internal UID
    KeyedStream<AtomicSettlementContext, UUID> keyedReadySettlementContexts =
        readySettlementContexts.keyBy(AtomicSettlementContext::getInternalUid);
    DataStream<AtomicSettlementContext> completedSettlementContexts =
        keyedReadySettlementContexts
            .connect(keyedPaymentConfirmationContexts)
            .flatMap(new JoinPaymentConfirmations());

    if (trace) {
      completedSettlementContexts
          .map(
              context -> {
                ForeignExchangeTradeInstructionV04 request = context.getOriginalMessage();
                log.info(
                    "--> Sent fxtr.017, Foreign Exchange Trade Status And Details Notification: bought {} {} for {} {}",
                    request.getTradingSideBuyAmountCurrency(),
                    request.getTradingSideBuyAmount().longValue(),
                    request.getTradingSideSellAmountCurrency(),
                    request.getTradingSideSellAmount().longValue());
                return request;
              })
          .addSink(new DiscardingSink<>());
    }

    if (printThroughput || printLatency) {
      completedSettlementContexts
          .map(context -> Duration.between(context.getStartTimestamp(), Instant.now()).toMillis())
          .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(outputFreqSecs)))
          .aggregate(
              new AggregateFunction<Long, MinMaxSumCount, MinMaxSumCount>() {
                @Override
                public MinMaxSumCount createAccumulator() {
                  return new MinMaxSumCount();
                }

                @Override
                public MinMaxSumCount add(Long value, MinMaxSumCount accumulator) {
                  return accumulator.update(value);
                }

                @Override
                public MinMaxSumCount getResult(MinMaxSumCount accumulator) {
                  return accumulator;
                }

                @Override
                public MinMaxSumCount merge(MinMaxSumCount a, MinMaxSumCount b) {
                  return a.add(b);
                }
              },
              new AllWindowFunction<MinMaxSumCount, Void, TimeWindow>() {
                @Override
                public void apply(
                    TimeWindow window, Iterable<MinMaxSumCount> values, Collector<Void> out)
                    throws Exception {
                  MinMaxSumCount stats =
                      StreamSupport.stream(values.spliterator(), false)
                          .reduce(MinMaxSumCount::add)
                          .orElseThrow(() -> new IllegalStateException("no data"));

                  long minLatency = stats.min;
                  long maxLatency = stats.max;
                  float avgLatency = stats.sum / (float) stats.count;
                  float avgThroughput = stats.count / (float) outputFreqSecs;

                  if (printThroughput) {
                    log.info(
                        "Completed atomic settlements: {}/sec",
                        String.format("%.2f", avgThroughput));
                  }

                  if (printLatency) {
                    log.info("End-to-end latency (including calls to external systems):");
                    log.info("- min: {} ms", minLatency);
                    log.info("- max: {} ms", maxLatency);
                    log.info("- avg: {} ms", String.format("%.1f", avgLatency));
                  }

                  log.info("");
                }
              })
          .addSink(new DiscardingSink<>());
    }

    env.execute(AtomicSettlement.class.getSimpleName());
  }

  @Data
  private static class MinMaxSumCount {
    private long min = Integer.MAX_VALUE;
    private long max = Integer.MIN_VALUE;
    private long sum = 0;
    private long count = 0;

    public MinMaxSumCount update(long value) {
      if (value < min) {
        min = value;
      }

      if (value > max) {
        max = value;
      }

      sum += value;
      count += 1;

      return this;
    }

    public MinMaxSumCount add(MinMaxSumCount other) {
      if (other.min < min) {
        min = other.min;
      }

      if (other.max < max) {
        max = other.max;
      }

      sum += other.sum;
      count += other.count;

      return this;
    }
  }
}
