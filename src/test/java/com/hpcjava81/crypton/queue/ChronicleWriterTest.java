package com.hpcjava81.crypton.queue;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.util.TestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

public class ChronicleWriterTest {
    private ChronicleWriter w;
    private String queuePath;

    @Before
    public void before() throws Exception {
        queuePath = "./test/data/writer/1";
        w = new ChronicleWriter(queuePath);
    }

    @Test
    public void testConstructed() throws Exception {
        Assert.assertNotNull(w);
    }

    @Test
    public void testWriteOneBook() throws Exception {
        w.write(createNewBook());

        //wait for the writer thread to do the write
        Thread.sleep(1000);

        String dump = w.getQueue().dump();
        System.out.println(dump);
        Assert.assertNotNull(dump);
    }

    @NotNull
    private static OrderBook createNewBook() {
        OrderBook book = new OrderBook("SYM1");
        book.update(100, 10, 1, true);
        book.update(99, 10, 2, true);
        book.update(98, 10, 3, true);
        book.update(101, 10, 4, false);
        book.update(102, 10, 5, false);
        book.update(103, 10, 6, false);

        book.setTickSizes(100, 100);
        return book;
    }

    @After
    public void after() throws Exception {
        w.close();
        TestUtil.deleteFilesIn(Paths.get(queuePath));
    }

}
