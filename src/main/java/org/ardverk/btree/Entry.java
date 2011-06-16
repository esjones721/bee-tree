package org.ardverk.btree;

import java.util.Map;

class Entry<K, V> implements Map.Entry<K, V> {

    private final K key;
    
    private final V value;
    
    private final Node.Id next;
    
    public Entry(K key, V value) {
        this(key, value, null);
    }
    
    public Entry(K key, Node.Id next) {
        this(key, null, next);
    }
    
    private Entry(K key, V value, Node.Id next) {
        if (next != null && value != null) {
            throw new IllegalStateException();
        }
        
        this.key = key;
        this.value = value;
        this.next = next;
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
    
    public Node.Id getNextId() {
        return next;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        
        if (next != null) {
            sb.append(" -> ").append(next);
        } else {
            sb.append("=").append(value);
        }
        
        return sb.toString();
    }
}
