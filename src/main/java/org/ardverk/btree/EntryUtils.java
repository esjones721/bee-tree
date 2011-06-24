package org.ardverk.btree;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

class EntryUtils {

    private EntryUtils() {}
    
    public static <K> int binarySearch(List<? extends Map.Entry<? extends K, ?>> list, 
            K key, Comparator<? super K> comparator) {
        return binarySearch(list, 0, list.size(), key, comparator);
    }
    
    public static <K> int binarySearch(List<? extends Map.Entry<? extends K, ?>> list, 
            int offset, int length, K key, Comparator<? super K> comparator) {
        
        int low = 0;
        int high = length - 1;
        
        while (low <= high) {
            int mid = (low + high) >>> 1;
            
            Map.Entry<? extends K, ?> node = list.get(offset + mid);
            
            int cmp = comparator.compare(node.getKey(), key);

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
}
