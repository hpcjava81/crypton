package com.hpcjava81.crypton.algo.arb;

import com.hpcjava81.crypton.algo.StrategyInstrumentState;
import com.hpcjava81.crypton.book.L1OrderBookView;
import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.book.OrderBookChangeListener;
import com.hpcjava81.crypton.domain.Instrument;
import com.hpcjava81.crypton.util.ReusableObjPool;

public class LastSnapshotInstrumentState implements StrategyInstrumentState<TArb2Crypto1FiatStrat>, OrderBookChangeListener {
    private final Instrument instrument;
    private final L1OrderBookView l1OrderBook;
    private final ReusableObjPool<int[][]> pool;

    public LastSnapshotInstrumentState(Instrument instrument) {
        this.instrument = instrument;
        this.l1OrderBook = new L1OrderBookView(instrument);
        this.pool = new ReusableObjPool<>(() -> new int[1][4], 32);
    }

    @Override
    public L1OrderBookView l1OrderBook() {
        return l1OrderBook;
    }

    @Override
    public void onChange(OrderBook book) {
        int[][] toFill = null;
        try {
            toFill = pool.get();
            book.topNLevels(1, toFill);

            double bidPx = (double)toFill[0][1] / book.getPriceTickSize();
            double bidQty = (double)toFill[0][0] / book.getSizeTickSize();
            double askPx = (double)toFill[0][2] / book.getPriceTickSize();
            double askQty = (double)toFill[0][3] / book.getSizeTickSize();

            l1OrderBook.fill(bidPx, bidQty, askPx, askQty, System.currentTimeMillis());
        } finally {
            pool.release(toFill);
        }
    }

    public Instrument getInstrument() {
        return instrument;
    }
}
