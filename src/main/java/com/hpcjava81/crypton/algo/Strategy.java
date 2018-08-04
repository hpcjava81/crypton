package com.hpcjava81.crypton.algo;

/**
 * This represents a runnable <code>Algo</code> instance with possible
 * parameterisation. So we can have multiple running stratgies on the same algo,
 * for example, for different symbols, or different parameters, etc.
 *
 * @param <T> the <code>Algo</code> which defines this strategy
 */
public interface Strategy<T extends Algo> {

    //

}
