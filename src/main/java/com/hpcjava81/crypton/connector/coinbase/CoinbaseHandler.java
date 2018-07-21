package com.hpcjava81.crypton.connector.coinbase;

import com.google.gson.Gson;
import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.connector.ExchangeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CoinbaseHandler implements ExchangeHandler {
    private static final Logger log = LoggerFactory.getLogger(CoinbaseHandler.class);

    private final OrderBook book;
    private final int priceTickSize;
    private final int sizeTickSize;

    public CoinbaseHandler(OrderBook book, int priceTickSize, int sizeTickSize) {
        this.book = book;
        this.priceTickSize = priceTickSize;
        this.sizeTickSize = sizeTickSize;
    }

    @Override
    public void onMessage(String json) {
        if (log.isDebugEnabled()) {
            log.info(json);
        }

        if (isSnapshot(json)) {
            processSnapshot(json);
        } else if (isUpdate(json)) {
            processUpdate(json);
        }
    }

    private void processSnapshot(String json) {
        book.clear();

        Snapshot snapshot = new Gson().fromJson(json, Snapshot.class);
        long timestamp = System.currentTimeMillis();
        for (List<String> bid : snapshot.bids) {
            book.update(
                    toInt(bid.get(0), priceTickSize),
                    toInt(bid.get(1), sizeTickSize),
                    timestamp,
                    true
                    );
        }
        for (List<String> ask : snapshot.asks) {
            book.update(
                    toInt(ask.get(0), priceTickSize),
                    toInt(ask.get(1), sizeTickSize),
                    timestamp,
                    false
                    );
        }
    }

    private void processUpdate(String json) {
        L2Update l2Update = new Gson().fromJson(json, L2Update.class);
        long timestamp = System.currentTimeMillis();
        for (List<String> change : l2Update.changes) {
            boolean bid = "buy".equals(change.get(0));
            int size = toInt(change.get(2), sizeTickSize);
            int price = toInt(change.get(1), priceTickSize);
            if (size == 0) {
                book.remove(price, bid);
            } else {
                book.update(price, size, timestamp, bid);
            }
        }
    }

    private static int toInt(String str, int tickSize) {
        return (int) (Double.valueOf(str) * tickSize);
    }

    private boolean isSnapshot(String json) {
        return json.contains("snapshot");
    }

    private boolean isUpdate(String json) {
        return json.contains("l2update");
    }

    public static class Snapshot {
        public List<List<String>> bids;
        public List<List<String>> asks;
    }

    public static class L2Update {
        public List<List<String>> changes;
    }

}
