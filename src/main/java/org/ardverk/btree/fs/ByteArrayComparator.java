package org.ardverk.btree.fs;

import java.util.Comparator;

public class ByteArrayComparator implements Comparator<byte[]> {

    public static final ByteArrayComparator COMPARATOR 
        = new ByteArrayComparator();
    
    @Override
    public int compare(byte[] o1, byte[] o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else if (o2 == null) {
            return 1;
        }
        
        int length = o1.length;
        int diff = length - o2.length;
        if (diff != 0) {
            return diff;
        }
        
        for (int i = 0; i < length; i++) {
            diff = (o1[i] & 0xFF) - (o2[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }
        
        return 0;
    }
}
