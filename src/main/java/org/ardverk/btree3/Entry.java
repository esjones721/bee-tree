package org.ardverk.btree3;

import java.util.Map;

import org.ardverk.btree3.Node.Id;

class Entry<K, V> implements Map.Entry<K, V> {

    private final K key;
    
    private final V value;
    
    private final Node.Id nodeId;

    public Entry(K key, V value) {
        this(key, value, (Id)null);
    }
    
    public Entry(Entry<K, V> entry, Id nodeId) {
        this(entry.key, entry.value, nodeId);
    }
    
    public Entry(Entry<?, ?> entry) {
        this(null, null, entry.nodeId);
    }
    
    public Entry(K key, V value, Entry<?, ?> entry) {
        this(key, value, entry.nodeId);
    }
    
    public Entry(K key, V value, Id nodeId) {
        super();
        this.key = key;
        this.value = value;
        this.nodeId = nodeId;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    public Node.Id getNodeId() {
        return nodeId;
    }
    
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (key != null) {
            sb.append(key).append("=").append(value);
        } else if (nodeId != null) {
            sb.append("@");
        }
        
        return sb.toString();
    }
}
