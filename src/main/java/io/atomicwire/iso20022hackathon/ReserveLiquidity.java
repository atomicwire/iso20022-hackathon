package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.LiquidityReservationContext;
import io.atomicwire.iso20022hackathon.context.PaymentObligationContext;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

/**
 * A mock liquidity reservation operator that simulates reserving liquidity from a liquidity
 * provider (e.g. a central bank) by waiting a random duration between 500ms and 1000ms before
 * completing.
 */
public class ReserveLiquidity
    extends RichAsyncFunction<PaymentObligationContext, LiquidityReservationContext> {

  private transient ScheduledExecutorService scheduler;

  @Override
  public void open(Configuration parameters) {
    scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void close() {
    scheduler.shutdown();
  }

  @Override
  public void asyncInvoke(
      PaymentObligationContext input, ResultFuture<LiquidityReservationContext> resultFuture) {
    UUID internalUid = input.getInternalUid();
    PaymentObligation paymentObligation = input.getPaymentObligation();

    // Random duration between 500 and 100 ms
    long requestDurationMs = 500 + ThreadLocalRandom.current().nextInt(500);

    scheduler.schedule(
        () -> {
          LiquidityReservationContext liquidityReservationContext =
              new LiquidityReservationContext(internalUid, paymentObligation);
          resultFuture.complete(Collections.singleton(liquidityReservationContext));
        },
        requestDurationMs,
        TimeUnit.MILLISECONDS);
  }
}
