package com.hpcjava81.crypton;

import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.CoinbaseConnector;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        Connector coinbase = new CoinbaseConnector(new ArrayList<>());
        coinbase.start();

    }

}
