package io.atomicwire.iso20022hackathon;

import io.atomicwire.iso20022hackathon.context.AtomicSettlementContext;
import io.atomicwire.iso20022hackathon.context.LiquidityReservationContext;
import java.util.stream.StreamSupport;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;

public class JoinLiquidityReservations
    extends RichCoFlatMapFunction<
        AtomicSettlementContext, LiquidityReservationContext, AtomicSettlementContext> {

  private transient ValueState<AtomicSettlementContext> settlementContextState;
  private transient ListState<LiquidityReservationContext> liquidityReservationContextsState;

  @Override
  public void open(Configuration parameters) {
    ValueStateDescriptor<AtomicSettlementContext> settlementContextStateDescriptor =
        new ValueStateDescriptor<>("settlementContext", AtomicSettlementContext.class);
    settlementContextState = getRuntimeContext().getState(settlementContextStateDescriptor);

    ListStateDescriptor<LiquidityReservationContext> liquidityReservationContextsStateDescriptor =
        new ListStateDescriptor<>(
            "liquidityReservationContexts", LiquidityReservationContext.class);
    liquidityReservationContextsState =
        getRuntimeContext().getListState(liquidityReservationContextsStateDescriptor);
  }

  @Override
  public void flatMap1(AtomicSettlementContext value, Collector<AtomicSettlementContext> out)
      throws Exception {
    settlementContextState.update(value);
    emitIfAllLiquidityReserved(out);
  }

  @Override
  public void flatMap2(LiquidityReservationContext value, Collector<AtomicSettlementContext> out)
      throws Exception {
    liquidityReservationContextsState.add(value);
    emitIfAllLiquidityReserved(out);
  }

  /**
   * If the {@link AtomicSettlementContext} for this transaction along with all necessary {@link
   * LiquidityReservationContext}s have been collected, emit the settlement context and clear state.
   */
  private void emitIfAllLiquidityReserved(Collector<AtomicSettlementContext> out) throws Exception {
    AtomicSettlementContext settlementContext = settlementContextState.value();
    if (settlementContext == null) {
      return; // Still awaiting the settlement context
    }

    int expectedLiquidityReservations =
        settlementContext.getForeignExchangeTrade().getResultingSettlements().size();
    long receivedLiquidityReservations =
        StreamSupport.stream(liquidityReservationContextsState.get().spliterator(), false).count();

    if (receivedLiquidityReservations < expectedLiquidityReservations) {
      return; // Still awaiting at least one liquidity reservation
    }

    // The settlement context and all necessary liquidity reservations have arrived; emit the
    // context downstream and clear state
    out.collect(settlementContext);
    settlementContextState.clear();
    liquidityReservationContextsState.clear();
  }
}
