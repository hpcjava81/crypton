package com.hpcjava81.crypton.queue;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.ValueIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChronicleReader {
    private static final Logger log = LoggerFactory.getLogger(ChronicleReader.class);

    private final String queuePath;
    private final ChronicleQueue queue;
    private final ExcerptTailer tailer;

    private volatile boolean stop;
    private ExecutorService executor  = Executors.newFixedThreadPool(1);

    public ChronicleReader(String queuePath) {
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.tailer = queue.createTailer();
    }

    public void start() {
        executor.submit(() -> {
            //busy-spin
            while(!stop) {
                tailer.readDocument(w -> {
                    ValueIn in = w.getValueIn();

                    long timestamp = in.int64();
                    float priceTick = in.float32();
                    float sizeTick = in.float32();

                    //TODO add all fields

//                    log.info(timestamp + "|" + priceTick + "|" + sizeTick);

                    //TODO

                });
            }
        });
    }

    public void stop() {
        stop = true;
    }

    public void close() {
        this.queue.close();
    }

}
