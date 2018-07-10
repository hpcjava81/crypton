package com.hpcjava81.crypton;

import com.hpcjava81.crypton.connector.Connector;
import com.hpcjava81.crypton.connector.GdaxConnector;

public class Main {

    public static void main(String[] args) {

        Connector gdax = new GdaxConnector();
        gdax.start();

    }

}
