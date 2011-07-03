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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

abstract class AbstractBeeTree<K, V> implements BST<K, V> {

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        return iterator(key, true);
    }
    
    @Override
    public String toString() {
        return toString(10);
    }
    
    public String toString(int max) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        Iterator<Entry<K, V>> it = iterator();
        if (it.hasNext()) {
            for (int i = 0; i < max && it.hasNext(); i++) {
                sb.append(it.next()).append(", ");
            }
            
            if (it.hasNext()) {
                sb.append(size() - max).append(" more...");
            } else {
                sb.setLength(sb.length()-2);
            }
        }
        
        sb.append("]");
        
        return sb.toString();
    }
}