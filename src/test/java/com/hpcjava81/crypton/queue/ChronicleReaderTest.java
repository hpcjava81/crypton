package com.hpcjava81.crypton.queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChronicleReaderTest {
    private static final String ETHUSD = "SYM1";

    private String queuePath;
    private ChronicleReader r;

    @Before
    public void before() throws Exception {
        queuePath = "./test/data/" + ETHUSD;
        r = new ChronicleReader(ETHUSD, queuePath);
    }

    @Test
    public void testConstructed() throws Exception {
        Assert.assertNotNull(r);
    }

    @Test
    public void testRead() throws Exception {
        r.start();

        Thread.sleep(1000);

        r.stop();
    }


}
