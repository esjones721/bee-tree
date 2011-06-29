package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractBeeTree<K, V> implements BST<K, V> {

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
        sb.append("]");
        
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
