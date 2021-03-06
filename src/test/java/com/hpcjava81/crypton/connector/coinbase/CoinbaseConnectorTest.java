package com.hpcjava81.crypton.connector.coinbase;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.book.OrderBookChangeListener;
import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.ExchangeHandler;
import com.hpcjava81.crypton.domain.Instrument;
import com.hpcjava81.crypton.queue.ChronicleWriter;
import com.hpcjava81.crypton.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoinbaseConnectorTest {
    private static final Instrument ETHUSD = new Instrument("ETH-USD");

    private ExchangeHandler handler;
    private OrderBook orderBook;
    private ChronicleWriter writer;
    private String queuePath;

    @Before
    public void before() {
        this.queuePath = "./test/data/" + ETHUSD.getName();
        this.writer = new ChronicleWriter(queuePath);
        this.orderBook = new OrderBook(ETHUSD);
        this.handler = new CoinbaseHandler(orderBook,
                new OrderBookChangeListener[]{writer}, 100,10000);
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
        Connector coinbase = new CoinbaseConnector(
                Collections.singletonList(ETHUSD.getName()), handler);
        coinbase.start();

        Thread.sleep(5000);

        coinbase.stop();
    }

    @After
    public void after() throws Exception {
        writer.close();
        TestUtil.deleteFilesIn(Paths.get(queuePath));
    }
}
