package com.hpcjava81.crypton.algo;

import com.hpcjava81.crypton.domain.Instrument;

/**
 * Holds the state corresponding to an instrument which is needed for a strategy.
 * The state could be simply last order book snapshot, for example.
 *
 * @param <S> the strategy which uses this state
 * @param <I> the instrument which this state belongs to
 */
public interface StrategyInstrumentState<S extends Strategy, I extends Instrument> {

    //

}
