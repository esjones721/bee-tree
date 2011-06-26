package org.ardverk.btree.io;

import java.io.Closeable;
import java.io.IOException;

public class IoUtils {

    private IoUtils() {}

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException err) {}
        }
    }
}
