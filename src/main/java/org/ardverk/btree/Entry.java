package org.ardverk.btree;

import java.util.Map;

class Entry<K, V> implements Map.Entry<K, V> {

    private final K key;
    
    private final V value;
    
    private final Node.Id nodeId;
    
    public Entry(K key, V value) {
        this(key, value, null);
    }
    
    public Entry(K key, Node.Id nodeId) {
        this(key, null, nodeId);
    }
    
    private Entry(K key, V value, Node.Id nodeId) {
        if (nodeId != null && value != null) {
            throw new IllegalStateException();
        }
        
        this.key = key;
        this.value = value;
        this.nodeId = nodeId;
    }

    public boolean isLeaf() {
        return nodeId == null;
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
    
    public Node.Id getNodeId() {
        return nodeId;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        
        if (isLeaf()) {
            sb.append("=").append(value);
        } else {
            sb.append(" -> ").append(nodeId);
        }
        
        return sb.toString();
    }
}
