package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractBeeTree<K, V> implements BST<K, V> {

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        return iterator(key, true);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        if (!isEmpty()) {
            for (Map.Entry<K, V> entry : this) {
                sb.append(entry).append(", ");
            }
            
            sb.setLength(sb.length()-2);
        }
        
        sb.append("]");
        return sb.toString();
    }
}
