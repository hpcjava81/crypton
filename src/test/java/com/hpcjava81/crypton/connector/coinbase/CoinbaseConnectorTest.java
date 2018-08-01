package com.hpcjava81.crypton.connector.coinbase;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.ExchangeHandler;
import com.hpcjava81.crypton.queue.ChronicleWriter;
import com.hpcjava81.crypton.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoinbaseConnectorTest {
    private static final String ETHUSD = "ETH-USD";

    private ExchangeHandler handler;
    private OrderBook orderBook;
    private ChronicleWriter writer;
    private String queuePath;

    @Before
    public void before() {
        this.queuePath = "./test/data/" + ETHUSD;
        this.writer = new ChronicleWriter(queuePath);
        this.orderBook = new OrderBook(ETHUSD);
        this.handler = new CoinbaseHandler(orderBook, writer, 100,10000);
    }

    @Test
    public void testConstruction() throws Exception {
        List<String> symbols = new ArrayList<>();
        symbols.add("a");

        Connector coinbase = new CoinbaseConnector(symbols, handler);
        Assert.assertNotNull(coinbase);
    }

    @Test
    public void testToCsv() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        String csv = CoinbaseConnector.toCsv(list);
        Assert.assertEquals("\"a\",\"b\",\"c\"", csv);
    }

    @Test
    public void testConnection() throws Exception {
        Connector coinbase = new CoinbaseConnector(Collections.singletonList(ETHUSD), handler);
        coinbase.start();

        Thread.sleep(5000);

        coinbase.stop();

        int[][] dump = orderBook.dump();
        for (int[] aDump : dump) {
            System.out.println(Arrays.toString(aDump));
        }

        System.out.println("--------QUEUE DUMP----------");
        System.out.println(writer.getQueue().dump());
    }

    @After
    public void after() throws Exception {
        writer.close();
        TestUtil.deleteFilesIn(Paths.get(queuePath));
    }
}
