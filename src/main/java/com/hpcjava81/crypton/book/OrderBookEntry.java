package com.hpcjava81.crypton.book;

class OrderBookEntry {
    private int size;
    private long timestamp;

    void fill(int size, long timestamp) {
        this.size = size;
        this.timestamp = timestamp;
    }

    public int getSize() {
        return size;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
