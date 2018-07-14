package com.hpcjava81.crypton.util;

import java.util.Collection;

public class Args {

    public static <E> void requireNonEmpty(Collection<E> c, String msg) {
        if (c == null || c.size() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }

}
