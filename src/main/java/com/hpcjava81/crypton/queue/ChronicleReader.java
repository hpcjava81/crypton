package com.hpcjava81.crypton.queue;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.ValueIn;

public class ChronicleReader {

    private final String queuePath;
    private final ChronicleQueue queue;
    private final ExcerptTailer tailer;


    public ChronicleReader(String queuePath) {
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.tailer = queue.createTailer();
    }

    public void read() {
        tailer.readDocument(w -> {
            ValueIn in = w.getValueIn();

            long timestamp = in.int64();
            float priceTick = in.float32();
            float sizeTick = in.float32();


        });

        throw new UnsupportedOperationException();
    }

    public void close() {
        this.queue.close();
    }

}
