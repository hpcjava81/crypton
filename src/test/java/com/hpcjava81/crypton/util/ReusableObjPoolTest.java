package com.hpcjava81.crypton.util;

import org.junit.Assert;
import org.junit.Test;

public class ReusableObjPoolTest {

    @Test
    public void testPoolReuses() throws Exception {
        ReusableObjPool<TestObj> pool = new ReusableObjPool<>(TestObj::new, 2);

        TestObj o1;
        TestObj o2;
        TestObj o3;

        o1 = pool.get();
        Assert.assertNotNull(o1);

        o2 = pool.get(); //the pool should be empty after this
        Assert.assertNotNull(o2);

        pool.release(o1); //release back o1
        o3 = pool.get(); //this should be o1 returned back
        Assert.assertTrue(o1 == o3); //object identity test
    }

    @Test
    public void testPoolGrows() throws Exception {
        ReusableObjPool<TestObj> pool = new ReusableObjPool<>(TestObj::new, 2);

        TestObj o1;
        for(int i=0; i<100; i++) {
            o1 = pool.get();
            Assert.assertNotNull(o1);

            if (i >= 10 * 2) { //thresholdFactor breached
                //we should now be in defunct pool state - so pool size should be zero
                Assert.assertEquals(0, pool.poolSize());
            }
        }
    }

    private static class TestObj implements ReusableObjPool.Reusable{
        @Override
        public void reset() {
            //reset
        }
    }

}
