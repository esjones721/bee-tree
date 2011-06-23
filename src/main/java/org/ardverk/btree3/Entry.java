package org.ardverk.btree3;

import java.util.Map;

class Entry<K, V> implements Map.Entry<K, V> {

    private final K key;
    
    private final V value;
    
    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }
    
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
