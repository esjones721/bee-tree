/*
 * Copyright 2011 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
