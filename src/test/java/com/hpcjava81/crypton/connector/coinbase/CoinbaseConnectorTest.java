package com.hpcjava81.crypton.connector.coinbase;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.ExchangeHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoinbaseConnectorTest {
    private static final String ETHUSD = "ETH-USD";

    private ExchangeHandler handler;
    private OrderBook orderBook;

    @Before
    public void before() {
        this.orderBook = new OrderBook(ETHUSD);
        this.handler = new CoinbaseHandler(orderBook,100,10000);
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

        Thread.sleep(500000);

        coinbase.stop();

        System.out.println(orderBook.prettyPrint());
    }
}
