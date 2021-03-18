package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.PaymentConfirmationContext;
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
 * A mock payment settlement operator that simulates instructing a payment between accounts at a
 * place of settlement (e.g. a central bank) and waiting for the confirmation by waiting a random
 * duration between 0.5 and 5.0 seconds before completing.
 */
public class SettleTransaction
    extends RichAsyncFunction<PaymentObligationContext, PaymentConfirmationContext> {

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
      PaymentObligationContext input, ResultFuture<PaymentConfirmationContext> resultFuture) {
    UUID internalUid = input.getInternalUid();
    PaymentObligation paymentObligation = input.getPaymentObligation();

    // Random duration between 0.5s and 5s
    long requestDurationMs = 500 + ThreadLocalRandom.current().nextInt(5000 - 500);

    scheduler.schedule(
        () -> {
          PaymentConfirmationContext paymentConfirmationContext =
              new PaymentConfirmationContext(internalUid, paymentObligation);
          resultFuture.complete(Collections.singleton(paymentConfirmationContext));
        },
        requestDurationMs,
        TimeUnit.MILLISECONDS);
  }
}
