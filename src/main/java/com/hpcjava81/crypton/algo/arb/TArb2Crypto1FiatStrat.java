package com.hpcjava81.crypton.algo.arb;

import com.hpcjava81.crypton.algo.Strategy;
import com.hpcjava81.crypton.algo.StrategyInstrumentState;
import com.hpcjava81.crypton.domain.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TArb2Crypto1FiatStrat implements Strategy<TArb> {
    private static final Logger log = LoggerFactory.getLogger(TArb2Crypto1FiatStrat.class);

    private static final double TOL = 1e-2;

    private final Instrument c1f1;
    private final Instrument c2f1;
    private final Instrument c1c2;

    private final StrategyInstrumentState<TArb2Crypto1FiatStrat> c1f1State;
    private final StrategyInstrumentState<TArb2Crypto1FiatStrat> c2f1State;
    private final StrategyInstrumentState<TArb2Crypto1FiatStrat> c1c2State;

    private final ScheduledExecutorService executor;

    public TArb2Crypto1FiatStrat(Instrument c1f1,
                                 Instrument c2f1,
                                 Instrument c1c2,
                                 StrategyInstrumentState<TArb2Crypto1FiatStrat> c1f1State,
                                 StrategyInstrumentState<TArb2Crypto1FiatStrat> c2f1State,
                                 StrategyInstrumentState<TArb2Crypto1FiatStrat> c1c2State) {
        this.c1f1 = c1f1;
        this.c2f1 = c2f1;
        this.c1c2 = c1c2;

        this.c1f1State = c1f1State;
        this.c2f1State = c2f1State;
        this.c1c2State = c1c2State;

        this.executor = Executors.newScheduledThreadPool(1);
    }

    public void runStrat() {
        executor.scheduleAtFixedRate(() -> {

            //start with 1 f1
            //buy c1 using c1f1 => 1/c1f1:askPx
            //sell c1 using c1c2 => c1c2:bidPx * (1/c1f1:askPx)
            //buy f1 using c2f1 => c2f1:bidPx * c1c2:bidPx * (1/c1f1:askPx)
            //c2f1:bidPx * c1c2:bidPx * (1/c1f1:askPx) > 1 ?

            double pnl = c2f1State.l1OrderBook().getBidPx()
                    * c1c2State.l1OrderBook().getBidPx()
                    * (1 / c1f1State.l1OrderBook().getAskPx());
            if (pnl - 1 > TOL) {

                //only log for now :-)
                log.info("TArb: " + (pnl-1) + " =>"
                        + c1f1State.l1OrderBook() + "|"
                        + c2f1State.l1OrderBook() + "|"
                        + c1c2State.l1OrderBook()
                );
            } else {
                if (pnl-1 > 1e-4) {
                    log.info("TArb: " + (pnl-1) + " (No trade)");
                }
            }
        }, 500, 50, TimeUnit.MILLISECONDS);
    }
}
