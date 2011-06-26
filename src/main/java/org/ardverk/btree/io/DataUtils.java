package org.ardverk.btree.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DataUtils {

    private DataUtils() {}

    public static void writeBytes(DataOutput out, byte[] data) throws IOException {
        int length = (data != null ? data.length : 0);
        out.writeInt(length);
        
        if (0 < length) {
            out.write(data);
        }
    }

    public static byte[] readBytes(DataInput in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }
    
    
}
