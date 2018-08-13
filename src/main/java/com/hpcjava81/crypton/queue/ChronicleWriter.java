package com.hpcjava81.crypton.queue;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.book.OrderBookChangeListener;
import com.hpcjava81.crypton.util.ReusableObjPool;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.WireOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronicleWriter implements OrderBookChangeListener {
    private static final Logger log = LoggerFactory.getLogger(ChronicleWriter.class);

    static final int MAX_LEVELS = 50;

    private final String queuePath;
    private final ChronicleQueue queue;
    private final ReusableObjPool<int[][]> pool;

    public ChronicleWriter(final String queuePath) {
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.pool = new ReusableObjPool<>(() -> new int[MAX_LEVELS][4], 128);
    }

    @Override
    public void onChange(OrderBook book) {
        write(book);
    }

    void write(final OrderBook book) {
        //Note: we acquire an appender on every call. This is done as otherwise
        //writing from multi-threads using the same appender doesn't work.
        //Internally it seems Chronicle keeps a thread local cache of appenders
        //so this shouldn't result in a new object creation every time.
        write0(book, queue.acquireAppender());
    }

    private void write0(OrderBook book, ExcerptAppender appender) {
        try {
            appender.writeDocument(w -> encode(book, w));
        } catch (Throwable e) {
            log.error("Error writing to queue " + queuePath, e);
            //TODO rethrow?
        }
    }

    private void encode(OrderBook book, WireOut m) {
        /*
         * this is 3x faster than encode0() below and uses
         * 7x less memory.
         *
         * size = 8 + 4 + 4 + 50*(4*4) = 816 bytes per message
        */
        m.getValueOut().int64(System.currentTimeMillis())
                .getValueOut().float32(book.getPriceTickSize())
                .getValueOut().float32(book.getSizeTickSize());

        int[][] toFill = null;
        try {
            toFill = pool.get();
            int levels = book.topNLevels(MAX_LEVELS, toFill);
            m.getValueOut().int32(levels);
            for (int i=0; i<levels; i++) {
                m.getValueOut().int32(toFill[i][1])
                        .getValueOut().int32(toFill[i][0])
                        .getValueOut().int32(toFill[i][2])
                        .getValueOut().int32(toFill[i][3]);
            }
        } finally {
            //no reset needed as we always know how many levels have data
            pool.release(toFill);
        }
    }

    @SuppressWarnings("unsued")
    private void encode0(OrderBook book, WireOut m) {
        m.write("timestamp").int64(System.currentTimeMillis())
                .write("priceTick").float32(book.getPriceTickSize())
                .write("sizeTick").float32(book.getSizeTickSize());

        int[][] toFill = null;
        try {
            toFill = pool.get();
            int levels = book.topNLevels(MAX_LEVELS, toFill);
            for (int i = 0; i < levels; i++) {
                m.write("bidPx" + i).int32(toFill[i][1])
                        .write("bidQty" + i).int32(toFill[i][0])
                        .write("askPx" + i).int32(toFill[i][2])
                        .write("askQty" + i).int32(toFill[i][3]);
            }
        } finally {
            pool.release(toFill);
        }
    }

    public String getQueuePath() {
        return queuePath;
    }

    ChronicleQueue getQueue() {
        return queue;
    }

    public void close() {
        queue.close();
    }
}
