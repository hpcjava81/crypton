package com.hpcjava81.crypton.book;

import it.unimi.dsi.fastutil.ints.*;

public class OrderBook {

    private final Int2ObjectSortedMap<OrderBookEntry> bids =
            new Int2ObjectRBTreeMap<>(IntComparators.OPPOSITE_COMPARATOR);
    private final Int2ObjectSortedMap<OrderBookEntry> asks =
            new Int2ObjectRBTreeMap<>(IntComparators.NATURAL_COMPARATOR);

    private final String symbol;

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void update(int price, int size, long timestamp, boolean bid) {
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
        if (bid) {
            bids.remove(price);
        } else {
            asks.remove(price);
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void clear() {
        bids.clear();
        asks.clear();
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("bids: ").append(bids.keySet().size())
                .append(", asks: ").append(asks.keySet().size());

        return sb.toString();
    }
}
