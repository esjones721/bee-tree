package org.ardverk.btree;

import java.util.Map;

public abstract class AbstractBeeTree<K, V> implements BeeTree<K, V> {

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
