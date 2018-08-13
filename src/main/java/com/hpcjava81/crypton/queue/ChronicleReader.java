package com.hpcjava81.crypton.queue;

import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.ValueIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hpcjava81.crypton.queue.ChronicleWriter.MAX_LEVELS;

public class ChronicleReader {
    private static final Logger log = LoggerFactory.getLogger(ChronicleReader.class);

    private static final String SERVER_IP = "127.0.0.1";
    private static final String KEYSPACE = "crypton";
    private static final int BATCH_SIZE = 200;


    private String symbol;
    private final String queuePath;
    private final ChronicleQueue queue;
    private final ExcerptTailer tailer;
    private final Session session;
    private final int[] bidAsks = new int[MAX_LEVELS*4];
    private final BatchStatement batchStatement;

    private volatile boolean stop;
    private ExecutorService executor  = Executors.newFixedThreadPool(1);
    private final PreparedStatement insertStatment;


    ChronicleReader(String symbol, String queuePath) {
        this.symbol = symbol;
        this.queuePath = queuePath;
        this.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        this.tailer = queue.createTailer();

        Cluster cluster = Cluster.builder()
                .addContactPoints(SERVER_IP)
                .build();
        cluster.getConfiguration().getCodecRegistry().register(InstantCodec.instance);

        session = cluster.connect(KEYSPACE);
        insertStatment = session.prepare(insertOderBookQuery());
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);

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

                    Arrays.fill(bidAsks, 0);
                    for (int i=0; i < levels ; i++){
                        bidAsks[i*4]=in.int32();
                        bidAsks[i*4+1]=in.int32();
                        bidAsks[i*4+2]=in.int32();
                        bidAsks[i*4+3]=in.int32();
                    }
                    BoundStatement bound = insertStatment.bind(symbol, priceTick, sizeTick, Instant.ofEpochMilli(timestamp),
                            bidAsks[0], bidAsks[1], bidAsks[2], bidAsks[3], bidAsks[4], bidAsks[5], bidAsks[6], bidAsks[7], bidAsks[8], bidAsks[9],
                            bidAsks[10], bidAsks[11], bidAsks[12], bidAsks[13], bidAsks[14], bidAsks[15], bidAsks[16], bidAsks[17], bidAsks[18], bidAsks[19],
                            bidAsks[20], bidAsks[21], bidAsks[22], bidAsks[23], bidAsks[24], bidAsks[25], bidAsks[26], bidAsks[27], bidAsks[28], bidAsks[29],
                            bidAsks[30], bidAsks[31], bidAsks[32], bidAsks[33], bidAsks[34], bidAsks[35], bidAsks[36], bidAsks[37], bidAsks[38], bidAsks[39],
                            bidAsks[40], bidAsks[41], bidAsks[42], bidAsks[43], bidAsks[44], bidAsks[45], bidAsks[46], bidAsks[47], bidAsks[48], bidAsks[49],
                            bidAsks[50], bidAsks[51], bidAsks[52], bidAsks[53], bidAsks[54], bidAsks[55], bidAsks[56], bidAsks[57], bidAsks[58], bidAsks[59],
                            bidAsks[60], bidAsks[61], bidAsks[62], bidAsks[63], bidAsks[64], bidAsks[65], bidAsks[66], bidAsks[67], bidAsks[68], bidAsks[69],
                            bidAsks[70], bidAsks[71], bidAsks[72], bidAsks[73], bidAsks[74], bidAsks[75], bidAsks[76], bidAsks[77], bidAsks[78], bidAsks[79],
                            bidAsks[80], bidAsks[81], bidAsks[82], bidAsks[83], bidAsks[84], bidAsks[85], bidAsks[86], bidAsks[87], bidAsks[88], bidAsks[89],
                            bidAsks[90], bidAsks[91], bidAsks[92], bidAsks[93], bidAsks[94], bidAsks[95], bidAsks[96], bidAsks[97], bidAsks[98], bidAsks[99],
                            bidAsks[100], bidAsks[101], bidAsks[102], bidAsks[103], bidAsks[104], bidAsks[105], bidAsks[106], bidAsks[107], bidAsks[108], bidAsks[109],
                            bidAsks[110], bidAsks[111], bidAsks[112], bidAsks[113], bidAsks[114], bidAsks[115], bidAsks[116], bidAsks[117], bidAsks[118], bidAsks[119],
                            bidAsks[120], bidAsks[121], bidAsks[122], bidAsks[123], bidAsks[124], bidAsks[125], bidAsks[126], bidAsks[127], bidAsks[128], bidAsks[129],
                            bidAsks[130], bidAsks[131], bidAsks[132], bidAsks[133], bidAsks[134], bidAsks[135], bidAsks[136], bidAsks[137], bidAsks[138], bidAsks[139],
                            bidAsks[140], bidAsks[141], bidAsks[142], bidAsks[143], bidAsks[144], bidAsks[145], bidAsks[146], bidAsks[147], bidAsks[148], bidAsks[149],
                            bidAsks[150], bidAsks[151], bidAsks[152], bidAsks[153], bidAsks[154], bidAsks[155], bidAsks[156], bidAsks[157], bidAsks[158], bidAsks[159],
                            bidAsks[160], bidAsks[161], bidAsks[162], bidAsks[163], bidAsks[164], bidAsks[165], bidAsks[166], bidAsks[167], bidAsks[168], bidAsks[169],
                            bidAsks[170], bidAsks[171], bidAsks[172], bidAsks[173], bidAsks[174], bidAsks[175], bidAsks[176], bidAsks[177], bidAsks[178], bidAsks[179],
                            bidAsks[180], bidAsks[181], bidAsks[182], bidAsks[183], bidAsks[184], bidAsks[185], bidAsks[186], bidAsks[187], bidAsks[188], bidAsks[189],
                            bidAsks[190], bidAsks[191], bidAsks[192], bidAsks[193], bidAsks[194], bidAsks[195], bidAsks[196], bidAsks[197], bidAsks[198], bidAsks[199]);
                    batchStatement.add(bound);
                    if(batchStatement.size() % BATCH_SIZE == 0){
                        session.executeAsync(batchStatement);
                        batchStatement.clear();
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
        session.close();
    }


    private String insertOderBookQuery() {
        return ("INSERT INTO orderBook(symbol, price_tick, size_tick, writeTimestamp, " +
                "bidPx0 , bidQty0, askPx0 , askQty0, bidPx1 , bidQty1, askPx1 , askQty1," +
                " bidPx2 , bidQty2,  askPx2,   askQty2,  bidPx3 ,  bidQty3,  askPx3 ,  askQty3, " +
                "bidPx4 , bidQty4,   askPx4,   askQty4,  bidPx5 ,  bidQty5,  askPx5 ,  askQty5," +
                " bidPx6 , bidQty6,  askPx6,   askQty6,  bidPx7 ,  bidQty7,  askPx7 ,  askQty7," +
                " bidPx8 , bidQty8,  askPx8,   askQty8,  bidPx9 ,  bidQty9,  askPx9 ,  askQty9, " +
                "bidPx10 , bidQty10, askPx10,  askQty10, bidPx11 , bidQty11, askPx11 , askQty11, " +
                "bidPx12 , bidQty12, askPx12,  askQty12, bidPx13 , bidQty13, askPx13 , askQty13, " +
                "bidPx14 , bidQty14, askPx14 , askQty14, bidPx15 , bidQty15, askPx15 , askQty15, " +
                "bidPx16 , bidQty16, askPx16 , askQty16, bidPx17 , bidQty17, askPx17 , askQty17, " +
                "bidPx18 , bidQty18, askPx18 , askQty18, bidPx19 , bidQty19, askPx19 , askQty19, " +
                "bidPx20 , bidQty20, askPx20 , askQty20, bidPx21 , bidQty21, askPx21 , askQty21, " +
                "bidPx22 , bidQty22, askPx22 , askQty22, bidPx23 , bidQty23, askPx23 , askQty23, " +
                "bidPx24 , bidQty24, askPx24 , askQty24, bidPx25 , bidQty25, askPx25 , askQty25, " +
                "bidPx26 , bidQty26, askPx26 , askQty26, bidPx27 , bidQty27, askPx27 , askQty27, " +
                "bidPx28 , bidQty28, askPx28 , askQty28, bidPx29 , bidQty29, askPx29 , askQty29, " +
                "bidPx30 , bidQty30, askPx30 , askQty30, bidPx31 , bidQty31, askPx31 , askQty31," +
                "bidPx32 , bidQty32, askPx32 , askQty32, bidPx33 , bidQty33, askPx33 , askQty33, " +
                "bidPx34 , bidQty34, askPx34 , askQty34, bidPx35 , bidQty35, askPx35 , askQty35, " +
                "bidPx36 , bidQty36, askPx36 , askQty36, bidPx37 , bidQty37, askPx37 , askQty37, " +
                "bidPx38 , bidQty38, askPx38 , askQty38, bidPx39 , bidQty39, askPx39 , askQty39, " +
                "bidPx40 , bidQty40, askPx40 , askQty40, bidPx41 , bidQty41, askPx41 , askQty41, " +
                "bidPx42 , bidQty42, askPx42 , askQty42, bidPx43 , bidQty43, askPx43 , askQty43, " +
                "bidPx44 , bidQty44, askPx44 , askQty44, bidPx45 , bidQty45, askPx45 , askQty45, " +
                "bidPx46 , bidQty46, askPx46 , askQty46, bidPx47 , bidQty47, askPx47 , askQty47, " +
                "bidPx48 , bidQty48, askPx48 , askQty48, bidPx49 , bidQty49, askPx49 , askQty49) " +
                "values(?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    }
}
