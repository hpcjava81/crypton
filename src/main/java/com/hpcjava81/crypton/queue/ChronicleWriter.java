package com.hpcjava81.crypton.queue;

import com.hpcjava81.crypton.book.OrderBook;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.WireOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ChronicleWriter {
    private static final Logger log = LoggerFactory.getLogger(ChronicleWriter.class);

    private final String queuePath;
    private final ChronicleQueue queue;
    private final ExcerptAppender appender;

    //need this as otherwise Chronicle complains about multi-threaded access.
    //this can be a perf issue due to context switch
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public ChronicleWriter(final String queuePath) {
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.appender = queue.acquireAppender();
    }

    public void write(final OrderBook book) {
        Future<?> fut = executor.submit(() -> {
            write0(book);
        });

        try {
            fut.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timed out waiting for chronicle write to complete: "
                    + queuePath, e);
        }
    }

    //pkg-pvt for tests
    void write0(OrderBook book) {
        try {
            this.appender.writeDocument(w ->
                    w.write(book.getSymbol())
                            .marshallable(m -> {
                                        encode(book, m);
                                    }
                            ));
        } catch (Throwable e) {
            log.error("Error writing to queue " + queuePath, e);
            //TODO rethrow?
        }
    }

    private void encode(OrderBook book, WireOut m) {
        /*
        this is 3x faster than encode0() below and uses
        7x less memory.
        size = 8 + 4 + 4 + 50*(4*4) = 816 bytes per message
         */

        m.getValueOut().int64(System.currentTimeMillis())
                .getValueOut().float32(book.getPriceTickSize())
                .getValueOut().float32(book.getSizeTickSize());

        int[][] levels = book.topNLevels(50);//TODO hard coded
        for (int[] level : levels) {
            m.getValueOut().int32(level[1])
                    .getValueOut().int32(level[0])
                    .getValueOut().int32(level[2])
                    .getValueOut().int32(level[3]);
        }
    }

    @SuppressWarnings("unsued")
    private void encode0(OrderBook book, WireOut m) {
        m.write("timestamp").int64(System.currentTimeMillis())
                .write("priceTick").float32(book.getPriceTickSize())
                .write("sizeTick").float32(book.getSizeTickSize());

        int[][] levels = book.topNLevels(50);//hard coded
        for (int i = 0; i < levels.length; i++) {
            m.write("bidPx" + i).int32(levels[i][1])
                    .write("bidQty" + i).int32(levels[i][0])
                    .write("askPx" + i).int32(levels[i][2])
                    .write("askQty" + i).int32(levels[i][3]);
        }
    }

    public String getQueuePath() {
        return queuePath;
    }

    public ChronicleQueue getQueue() {
        return queue;
    }

    public void close() {
        queue.close();
        executor.shutdownNow();
    }
}
