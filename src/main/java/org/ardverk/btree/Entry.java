package org.ardverk.btree;

import java.util.Map;

public class Entry<K, V> implements Map.Entry<K, V> {
    
    private final K key;
    
    private final V value;
    
    public Entry(Entry<K, V> exiting, V value) {
        this(exiting.key, value);
    }
    
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
        return key + " = " + value;
    }
}
