package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.context.LiquidityReservationContext;
import io.atomicwire.iso20022hackathon.context.PaymentContext;
import io.atomicwire.iso20022hackathon.context.PaymentObligationContext;
import io.atomicwire.iso20022hackathon.generator.ForeignExchangeTradeGenerator;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import io.atomicwire.iso20022hackathon.iso20022.logical.ForeignExchangeTradeInstructionV04;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

public class AtomicSettlement {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Generate a stream of simulated foreign exchange trade settlement requests
    DataStream<ForeignExchangeTradeInstructionV04> settlementRequests =
        env.addSource(new DataGeneratorSource<>(new ForeignExchangeTradeGenerator()))
            .returns(ForeignExchangeTradeInstructionV04.class);

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
            keyedPaymentObligationContexts, new ReserveLiquidity(), 1, TimeUnit.MINUTES, 100_000);

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
            new SettlePaymentObligation(),
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

    final int countFreqSec = 10;
    completedSettlementContexts
        .map(__ -> 1)
        .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(countFreqSec)))
        .sum(0)
        .map(count -> String.format("%s: %.1f/s", Instant.now(), count / (float) countFreqSec))
        .print();

    env.execute(AtomicSettlement.class.getSimpleName());
  }
}
