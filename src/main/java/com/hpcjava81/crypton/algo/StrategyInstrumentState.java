package com.hpcjava81.crypton.algo;

import com.hpcjava81.crypton.book.L1OrderBookView;

/**
 * Holds the state corresponding to an instrument which is needed for a strategy.
 * The state could be simply be last order book snapshot, for example.
 *
 * @param <S> the strategy which uses this state
 */
public interface StrategyInstrumentState<S extends Strategy> {

    L1OrderBookView l1OrderBook();

    //TODO

}
