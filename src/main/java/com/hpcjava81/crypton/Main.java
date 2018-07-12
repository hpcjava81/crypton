package com.hpcjava81.crypton;

import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.CoinbaseConnector;

public class Main {

    public static void main(String[] args) {

        Connector coinbase = new CoinbaseConnector();
        coinbase.start();

    }

}
