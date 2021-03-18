package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.flink.streaming.api.functions.async.AsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;

/**
 * A mock liquidity reservation operator that simulates reserving liquidity from a liquidity
 * provider (e.g. a central bank) by waiting a random duration between 0.5 and 5.0 seconds before
 * completing.
 */
public class ReserveLiquidity
    implements AsyncFunction<PaymentObligationContext, LiquidityReservationContext> {

  private static final Random RANDOM = ThreadLocalRandom.current();

  @Override
  public void asyncInvoke(
      PaymentObligationContext input, ResultFuture<LiquidityReservationContext> resultFuture) {
    UUID internalUid = input.getInternalUid();
    PaymentObligation paymentObligation = input.getPaymentObligation();

    // Random duration between 0.5s and 5s
    long requestDurationMs = 500 + RANDOM.nextInt(5000 - 500);

    CompletableFuture.supplyAsync(
            () -> {
              safeSleep(requestDurationMs);
              return true;
            })
        .thenAccept(
            success -> {
              LiquidityReservationContext liquidityReservationContext =
                  new LiquidityReservationContext(internalUid, paymentObligation);
              resultFuture.complete(Collections.singleton(liquidityReservationContext));
            });
  }

  private void safeSleep(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException ignored) {

    }
  }
}
