package com.hpcjava81.crypton.book;

import com.hpcjava81.crypton.domain.Instrument;

public final class L1OrderBookView {

    private final Instrument instrument;
    private double bidPx;
    private double bidQty;
    private double askPx;
    private double askQty;
    private long timestamp;

    public L1OrderBookView(Instrument instrument) {
        this.instrument = instrument;
    }

    public void fill(double bidPx, double bidQty, double askPx, double askQty,
                     long timestamp) {
        this.bidPx = bidPx;
        this.bidQty = bidQty;
        this.askPx = askPx;
        this.askQty = askQty;
        this.timestamp = timestamp;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public double getBidPx() {
        return bidPx;
    }

    public double getBidQty() {
        return bidQty;
    }

    public double getAskPx() {
        return askPx;
    }

    public double getAskQty() {
        return askQty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return instrument.getName() + ": "
                + bidQty + "," + bidPx + ","
                + askPx + "," + askQty;
    }
}
