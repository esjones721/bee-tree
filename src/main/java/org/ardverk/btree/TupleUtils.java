/*
 * Copyright 2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.btree;

import java.util.Comparator;
import java.util.List;

class TupleUtils {

    private TupleUtils() {}
    
    public static <K> int binarySearch(List<? extends Tuple> list, byte[] key) {
        return binarySearch(list, 0, list.size(), key);
    }
    
    public static <K> int binarySearch(List<? extends Tuple> list, 
            int offset, int length, byte[] key) {
        
        int low = 0;
        int high = length - 1;
        
        while (low <= high) {
            int mid = (low + high) >>> 1;
            
            Tuple entry = list.get(offset + mid);
            
            int cmp = compare(entry.getKey(), key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        
        return -(low + 1);  // key not found
    }
    
    public static int compare(byte[] o1, byte[] o2) {
        return ByteArrayComparator.COMPARATOR.compare(o1, o2);
    }
    
    private static class ByteArrayComparator implements Comparator<byte[]> {

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
}