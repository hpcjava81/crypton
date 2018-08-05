package com.hpcjava81.crypton.queue;

import com.hpcjava81.crypton.book.OrderBook;
import com.hpcjava81.crypton.domain.Instrument;
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

        String dump = w.getQueue().dump();
        System.out.println(dump);
        Assert.assertNotNull(dump);
    }

    @Test
    public void testWriteMultiOrderBook() throws Exception {
        OrderBook book = createNewBook();

        int N = 1_000_000;
        long start = System.nanoTime();
        for(int i = 0; i< N; i++) {
            w.write(book);
        }
        long took = System.nanoTime() - start;

        System.out.println("Took: " + (took/1e9) + " seconds");
        System.out.println((took/(N*1e3)) + "us per op");
    }

    @NotNull
    private static OrderBook createNewBook() {
        OrderBook book = new OrderBook(new Instrument("SYM1"));
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
