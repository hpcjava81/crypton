package com.hpcjava81.crypton.connector.coinbase;

import com.google.gson.Gson;
import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.connector.ExchangeHandler;
import com.hpcjava81.crypton.queue.ChronicleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CoinbaseHandler implements ExchangeHandler {
    private static final Logger log = LoggerFactory.getLogger(CoinbaseHandler.class);

    private final OrderBook book;
    private final ChronicleWriter writer;
    private final int priceTickSize;
    private final int sizeTickSize;
    private final Gson gson = new Gson();

    public CoinbaseHandler(OrderBook book, ChronicleWriter writer,
                           int priceTickSize, int sizeTickSize) {
        this.book = book;
        book.setTickSizes(priceTickSize, sizeTickSize);
        this.writer = writer;
        this.priceTickSize = priceTickSize;
        this.sizeTickSize = sizeTickSize;
    }

    @Override
    public void onMessage(String json) {
        //perf: no need to keep checking for this - first is always snapshot and
        //and only updates after that. Of course, if its not even an update then
        //we can to safeguard check if it was a snapshot. String.indexOf() is a
        //hot method.
        if (isSnapshot(json)) {
            processSnapshot(json);
        } else if (isUpdate(json)) {
            processUpdate(json);
        }
    }

    private void processSnapshot(String json) {
        book.clear();

        //perf: very hot method - takes about 20% overall time just to parse
        //(including the json parse on processUpdate())
        Snapshot snapshot = gson.fromJson(json, Snapshot.class);

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

        writer.write(book);
    }

    private void processUpdate(String json) {
        L2Update l2Update = gson.fromJson(json, L2Update.class);
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

        writer.write(book);
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
        private List<List<String>> bids;
        private List<List<String>> asks;

        public List<List<String>> getBids() {
            return bids;
        }

        public void setBids(List<List<String>> bids) {
            this.bids = bids;
        }

        public List<List<String>> getAsks() {
            return asks;
        }

        public void setAsks(List<List<String>> asks) {
            this.asks = asks;
        }
    }

    public static class L2Update {
        private List<List<String>> changes;

        public List<List<String>> getChanges() {
            return changes;
        }

        public void setChanges(List<List<String>> changes) {
            this.changes = changes;
        }
    }

}
