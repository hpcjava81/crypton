package com.hpcjava81.crypton.connector;

import com.hpcjava81.crypton.util.Args;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CoinbaseConnector implements Connector {
    private static final Logger log = LoggerFactory.getLogger(CoinbaseConnector.class);

    private static final String WSOC_URL = "wss://ws-feed.pro.coinbase.com";
    private static final String SUB_REQ = "{\n" +
            "    \"type\": \"subscribe\",\n" +
            "    \"product_ids\": [\n" +
            "        @@symbol-list@@\n" +
            "    ],\n" +
            "    \"channels\": [\n" +
            "        \"level2\"    ]\n" +
            "}\n";

    private final String subReq;

    private WebSocketClient client;

    private Callback callback;

    private CountDownLatch connectLatch;

    public CoinbaseConnector(List<String> symbols) {
        Args.requireNonEmpty(symbols, "symbol list null or empty");
        this.subReq = SUB_REQ.replace("@@symbol-list@@", toCsv(symbols));
    }

    //pkg-pvt for tests
    static String toCsv(List<String> symbols) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<symbols.size()-1;i++) {
            sb.append(wrapQuote(symbols.get(i))).append(",");
        }
        sb.append(wrapQuote(symbols.get(symbols.size()-1)));

        return sb.toString();
    }

    private static String wrapQuote(String s) {
        return "\"" + s + "\"";
    }

    public void start() {
        //connect
        connect();

        //subscribe
        callback.sendMessage(subReq);
        log.info("Subscribed to: " + WSOC_URL);
    }

    private void connect() {
        try {
            connectLatch = new CountDownLatch(1);

            client = new WebSocketClient(new SslContextFactory(true));
            client.start();

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect((callback = new Callback()), URI.create(WSOC_URL), request);

            //wait for connection
            connectLatch.await(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Error connecting to " + WSOC_URL, e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        //disconnect
        try {
            if (client != null) {
                client.stop();
            }
        } catch (Throwable t) {
            log.error("Error disconnecting " + WSOC_URL, t);
        }
    }

    public String desc() {
        return "Coinbase";
    }

    @WebSocket(maxTextMessageSize = Integer.MAX_VALUE)
    public class Callback {

        private Session session;

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            log.error(String.format("Connection closed: %d - %s%n",statusCode,reason));
            this.session = null;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            log.info("Got connect: %s%n",session);
            this.session = session;
            connectLatch.countDown();
        }

        void sendMessage(String message) {
            try {
                Future<Void> fut;
                fut = session.getRemote().sendStringByFuture(message);
                fut.get(2, TimeUnit.SECONDS); // wait for send to complete.
            } catch (Throwable t) {
                log.error("Error sending msg: " + message, t);
                throw new RuntimeException(t);
            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {
            //
            log.info(msg);
        }


    }
}
