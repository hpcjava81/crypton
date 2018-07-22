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

    public String prettyPrint() {
        if (bids.size() == 0 || asks.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\t\tBID\t\t").append("\t\tASK\t\t").append("\n");
        sb.append("\tSize\tPrice\t").append("|").append("\tSize\tPrice\t");
        sb.append("\n");

        IntBidirectionalIterator bidIter = bids.keySet().iterator();
        IntBidirectionalIterator askIter = asks.keySet().iterator();
        for(int i=0; i<10; i++) {
            int bid = bidIter.nextInt();
            sb.append("\t").append(bids.get(bid).getSize()).append("\t").append(bid);
            sb.append("\t|\t");
            int ask = askIter.nextInt();
            sb.append("\t").append(ask).append("\t").append(asks.get(ask).getSize());
            sb.append("\n");
        }

        return sb.toString();
    }
}
