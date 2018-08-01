package com.hpcjava81.crypton.book;

import it.unimi.dsi.fastutil.ints.*;

import java.util.concurrent.atomic.AtomicInteger;

public class OrderBook {

    private final Int2ObjectSortedMap<OrderBookEntry> bids =
            new Int2ObjectRBTreeMap<>(IntComparators.OPPOSITE_COMPARATOR);
    private final Int2ObjectSortedMap<OrderBookEntry> asks =
            new Int2ObjectRBTreeMap<>(IntComparators.NATURAL_COMPARATOR);

    private final String symbol;

    private final AtomicInteger lock = new AtomicInteger(0);
    private int priceTickSize;
    private int sizeTickSize;

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void update(int price, int size, long timestamp, boolean bid) {
        while (true) {
            if (lock.get() == 0) {
                if (lock.compareAndSet(0, 1)) {
                    try {
                        doUpdate(price, size, timestamp, bid);
                        return;
                    } finally {
                        lock.compareAndSet(1, 0);
                    }
                }
            }
        }
    }

    private void doUpdate(int price, int size, long timestamp, boolean bid) {
        OrderBookEntry obe;
        if (bid) {
            obe = bids.get(price);
            if (obe == null) {
                obe = new OrderBookEntry();
                bids.put(price, obe);
            }
        } else {
            obe = asks.get(price);
            if (obe == null) {
                obe = new OrderBookEntry();
                asks.put(price, obe);
            }
        }
        obe.fill(size, timestamp);
    }

    public void remove(int price, boolean bid) {
        while (true) {
            if (lock.get() == 0) {
                if (lock.compareAndSet(0, 1)) {
                    try {
                        if (bid) {
                            bids.remove(price);
                        } else {
                            asks.remove(price);
                        }
                        return;
                    } finally {
                        lock.compareAndSet(1, 0);
                    }
                }
            }
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public int[][] topNLevels(int N, int[][] toFill) {
        while (true) {
            if (lock.get() == 0) {
                if (lock.compareAndSet(0, 1)) {
                    try {
                        int bidSize = bids.size();
                        int askSize = asks.size();
                        if (bidSize == 0 || askSize == 0) {
                            return toFill;
                        }

                        IntBidirectionalIterator bidIter = bids.keySet().iterator();
                        IntBidirectionalIterator askIter = asks.keySet().iterator();

                        if (N < 0) {
                            N = Math.max(bidSize, askSize);
                        }

                        int pos = 0;
                        toFill = toFill == null ? new int[N][4] : toFill;
                        while (pos < N && (bidIter.hasNext() || askIter.hasNext())) {
                            if (bidIter.hasNext()) {
                                int bK = bidIter.nextInt();
                                toFill[pos][0] = bids.get(bK).getSize();
                                toFill[pos][1] = bK;
                            }

                            if (askIter.hasNext()) {
                                int aK = askIter.nextInt();
                                toFill[pos][2] = aK;
                                toFill[pos][3] = asks.get(aK).getSize();
                            }

                            pos++;
                        }

                        return toFill;
                    } finally {
                        lock.compareAndSet(1, 0);
                    }
                }
            }
        }
    }

    public void clear() {
        while (true) {
            if (lock.get() == 0) {
                if (lock.compareAndSet(0, 1)) {
                    try {
                        bids.clear();
                        asks.clear();
                        return;
                    } finally {
                        lock.compareAndSet(1, 0);
                    }
                }
            }
        }
    }

    public int[][] dump() {
        return topNLevels(-1, null); //get all
    }

    public void setTickSizes(int priceTickSize, int sizeTickSize) {
        this.priceTickSize = priceTickSize;
        this.sizeTickSize = sizeTickSize;
    }

    public int getPriceTickSize() {
        return priceTickSize;
    }

    public int getSizeTickSize() {
        return sizeTickSize;
    }
}
