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

import io.atomicwire.iso20022hackathon.context.LiquidityReservationContext;
import io.atomicwire.iso20022hackathon.context.PaymentObligationContext;
import io.atomicwire.iso20022hackathon.iso20022.conceptual.PaymentObligation;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

/**
 * A mock liquidity reservation operator that simulates reserving liquidity from a liquidity
 * provider (e.g. a central bank) by waiting a random duration between 500ms and 1000ms before
 * completing.
 */
@Slf4j
public class ReserveLiquidity
    extends RichAsyncFunction<PaymentObligationContext, LiquidityReservationContext> {

  private boolean trace = false;
  private transient ScheduledExecutorService scheduler;

  public ReserveLiquidity withTrace(boolean trace) {
    this.trace = trace;
    return this;
  }

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

    // Random duration between 500 and 1000 ms
    long requestDurationMs = 500 + ThreadLocalRandom.current().nextInt(500);

    if (trace) {
      log.info(
          "--> Sent camt.050, Liquidity Credit Transfer: reserve {} {} from {}",
          paymentObligation.getCurrency(),
          paymentObligation.getAmount().longValue(),
          paymentObligation.getDeliveryAgentBic());
    }

    scheduler.schedule(
        () -> {
          if (trace) {
            log.info(
                "<-- Received camt.025, Receipt: for liquidity reservation of {} {} from {}",
                paymentObligation.getCurrency(),
                paymentObligation.getAmount().longValue(),
                paymentObligation.getDeliveryAgentBic());
          }

          LiquidityReservationContext liquidityReservationContext =
              new LiquidityReservationContext(internalUid, paymentObligation, true);
          resultFuture.complete(Collections.singleton(liquidityReservationContext));
        },
        requestDurationMs,
        TimeUnit.MILLISECONDS);
  }
}
