package com.hpcjava81.crypton.queue;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.ValueIn;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChronicleReader {
    private static final Logger log = LoggerFactory.getLogger(ChronicleReader.class);
    private static final String DATABASE = "crypton";
    private static final String SERVER_IP = "127.0.0.1";
    private static final int BATCH_SIZE = 200;

    private String symbol;
    private final String queuePath;
    private final ChronicleQueue queue;
    private final ExcerptTailer tailer;
    private final InfluxDB influxDB;
    private int counter;

    private volatile boolean stop;
    private ExecutorService executor  = Executors.newFixedThreadPool(1);

    ChronicleReader(String symbol, String queuePath) {
        this.symbol = symbol;
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.tailer = queue.createTailer();

        influxDB = InfluxDBFactory.connect("http://"+SERVER_IP+":8086", "nouser", "nopassword");
        influxDB.createRetentionPolicy(
                "defaultPolicy", DATABASE, "30d", 1, true);

        counter=0;
    }

    void start() {
        executor.submit(() -> {
            //busy-spin
            while(!stop) {
                tailer.readDocument(w -> {
                    ValueIn in = w.getValueIn();

                    long timestamp = in.int64();
                    float priceTick = in.float32();
                    float sizeTick = in.float32();

                    int levels = in.int32();

                    Point.Builder memory = Point.measurement(symbol)
                            .time(timestamp, TimeUnit.MILLISECONDS);
                    memory.addField("priceTick", priceTick)
                            .addField("sizeTick", sizeTick);

                    for (int i=0; i < levels ; i++){
                        memory.addField("bidPrice"+i, in.int32());
                        memory.addField("bidSize"+i, in.int32());
                        memory.addField("askPrice"+i, in.int32());
                        memory.addField("askSize"+i, in.int32());
                    }
                    Point point = memory.build();

                    BatchPoints batchPoints = BatchPoints
                            .database(DATABASE)
                            .retentionPolicy("defaultPolicy")
                            .build();

                    batchPoints.point(point);
                    counter++;

                    if((counter > 0 )&& (counter % BATCH_SIZE ==0)){
                        influxDB.write(batchPoints);
                        batchPoints.getPoints().clear();
                    }
                    //TODO add all fields

//                    log.info(timestamp + "|" + priceTick + "|" + sizeTick);

                    //TODO

                });
            }
        });
    }

    void stop() {
        stop = true;
    }

    public void close() {
        this.queue.close();
    }

}
