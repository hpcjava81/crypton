package com.hpcjava81.crypton.algo.arb;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.book.OrderBookChangeListener;
import com.hpcjava81.crypton.connector.coinbase.CoinbaseConnector;
import com.hpcjava81.crypton.connector.coinbase.CoinbaseHandler;
import com.hpcjava81.crypton.domain.Instrument;
import com.hpcjava81.crypton.queue.ChronicleWriter;
import com.hpcjava81.crypton.util.TestUtil;
import org.junit.After;
import org.junit.Test;

import java.nio.file.Paths;

public class TArbRunner {
    private static final String queuePathBase = "./test/data/tarb";

    @Test
    public void testRunTArb() throws Exception {
        //ETH-USD, BTC-USD, ETH-BTC

        //instruments
        Instrument ethusd = new Instrument("ETH-USD");
        Instrument btcusd = new Instrument("BTC-USD");
        Instrument ethbtc = new Instrument("ETH-BTC");

        //order books
        OrderBook ethusdOB = new OrderBook(ethbtc);
        OrderBook btcusdOB = new OrderBook(btcusd);
        OrderBook ethbtcOB = new OrderBook(ethbtc);

        //tick sizes
        int ethusdPriceTick = 100;
        int ethusdSizeTick = 10000;
        int btcusdPriceTick = 100;
        int btcusdSizeTick = 10000;
        int ethbtcPriceTick = 100000;
        int ethbtcSizeTick = 10000;

        //writers
        ChronicleWriter ethusdW = new ChronicleWriter(queuePathBase + "/" + ethusd.getName());
        ChronicleWriter btcusdW = new ChronicleWriter(queuePathBase + "/" + btcusd.getName());
        ChronicleWriter ethbtcW = new ChronicleWriter(queuePathBase + "/" + ethbtc.getName());

        //strategy states
        LastSnapshotInstrumentState ethusdState = new LastSnapshotInstrumentState(ethusd);
        LastSnapshotInstrumentState btcusdState = new LastSnapshotInstrumentState(btcusd);
        LastSnapshotInstrumentState ethbtcState = new LastSnapshotInstrumentState(ethbtc);

        //handlers
        CoinbaseHandler ethusdHdlr = new CoinbaseHandler(ethusdOB, new OrderBookChangeListener[] {ethusdW, ethusdState}, ethusdPriceTick, ethusdSizeTick);
        CoinbaseHandler btcusdHdlr = new CoinbaseHandler(btcusdOB, new OrderBookChangeListener[] {btcusdW, btcusdState}, btcusdPriceTick, btcusdSizeTick);
        CoinbaseHandler ethbtcHdlr = new CoinbaseHandler(ethbtcOB, new OrderBookChangeListener[] {ethbtcW, ethbtcState}, ethbtcPriceTick, ethbtcSizeTick);

        //connectors
        CoinbaseConnector ethusdConn = new CoinbaseConnector(ethusd.getName(), ethusdHdlr);
        CoinbaseConnector btcusdConn = new CoinbaseConnector(btcusd.getName(), btcusdHdlr);
        CoinbaseConnector ethbtcConn = new CoinbaseConnector(ethbtc.getName(), ethbtcHdlr);

        //strats
        TArb2Crypto1FiatStrat strat = new TArb2Crypto1FiatStrat(ethusd, btcusd, ethbtc, ethusdState, btcusdState, ethbtcState);


        //start connectors
        ethusdConn.start();
        btcusdConn.start();
        ethbtcConn.start();

        //start Strat
        strat.runStrat();

        Thread.sleep(5000);
    }

    @After
    public void after() throws Exception {
        TestUtil.deleteFilesIn(Paths.get(queuePathBase + "/ETH-USD"));
        TestUtil.deleteFilesIn(Paths.get(queuePathBase + "/BTC-USD"));
        TestUtil.deleteFilesIn(Paths.get(queuePathBase + "/ETH-BTC"));
    }


}
