package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map.Entry;

abstract class AbstractBeeTree<K, V> implements BeeTree<K, V> {
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K key) {
        return iterator(key, true);
    }
}
