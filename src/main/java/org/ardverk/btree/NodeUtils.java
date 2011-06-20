package org.ardverk.btree;

import java.util.Comparator;
import java.util.List;

public class NodeUtils {

    private NodeUtils() {}
    
    public static <K> int binarySearch(List<? extends Node2<? extends K, ?>> list, 
            K key, Comparator<? super K> comparator) {
        
        int low = 0;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            
            Node2<? extends K, ?> node = list.get(mid);
            
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
