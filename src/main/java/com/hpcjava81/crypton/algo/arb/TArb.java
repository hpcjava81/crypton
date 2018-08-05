package com.hpcjava81.crypton.algo.arb;

import com.hpcjava81.crypton.algo.Algo;

public class TArb implements Algo {
    private static final double TOL = 1e-2;

    public static boolean runAlgo(double px1, double px2, double px3) {
        return Math.abs(px1/px2 - px3) > TOL;
    }

}
