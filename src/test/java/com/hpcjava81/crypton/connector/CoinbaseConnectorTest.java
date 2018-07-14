package com.hpcjava81.crypton.connector;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoinbaseConnectorTest {

    @Test
    public void testConstruction() throws Exception {
        List<String> symbols = new ArrayList<>();
        symbols.add("a");

        Connector coinbase = new CoinbaseConnector(symbols);
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
        Connector coinbase = new CoinbaseConnector(Collections.singletonList("ETH-USD"));
        coinbase.start();

        Thread.sleep(1000);

        coinbase.stop();
    }
}
