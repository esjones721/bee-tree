package org.ardverk.btree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree.NodeProvider.Intent;

class Node<K, V> implements Cloneable {

    private final NodeProvider<K, V> provider;
    
    private final boolean leaf;
    
    private final AtomicInteger referenceCount = new AtomicInteger(1);
    
    private final Node.Id nodeId = Node.Id.create();
    
    private final NavigableMap<K, Entry<K, V>> entries;
    
    private final int maxSize = 10;
    
    public Node(NodeProvider<K, V> provider, boolean leaf) {
        this.provider = provider;
        this.leaf = leaf;
        
        entries = new TreeMap<K, Entry<K,V>>(provider.comparator());
    }
    
    private Node(Node<K, V> src) {
        this(src.provider, src.leaf);
        
        for (Entry<K, V> entry : src.entries.values()) {
            Node<K, V> node = provider.get(
                    entry.getNodeId(), Intent.READ);
            node.incrementReferenceCount();
            
            entries.put(entry.getKey(), entry);
        }
    }
    
    public Id getNodeId() {
        return nodeId;
    }
    
    private boolean isFull() {
        return entries.size() >= maxSize;
    }
    
    private int getReferenceCount() {
        return referenceCount.get();
    }
    
    private int incrementReferenceCount() {
        return referenceCount.incrementAndGet();
    }
    
    private int decrementReferenceCount() {
        return referenceCount.decrementAndGet();
    }
    
    public int size() {
        return entries.size();
    }
    
    private Entry<K, V> ceilingValue(K key) {
        return entries.ceilingEntry(key).getValue();
    }
    
    public V put(K key, V value) {
        
        if (leaf && !isFull()) {
            
            Entry<K, V> element = new Entry<K, V>(key, value);
            Entry<K, V> existing = entries.put(key, element);
            
            return existing != null ? existing.getValue() : null;
        }
        
        Entry<K, V> closest = ceilingValue(key);
        
        Node.Id nodeId = closest.getNodeId();
        Node<K, V> child = provider.get(nodeId, Intent.WRITE);
        
        return child.put(key, value);
    }
    
    /*public Node<K, V>[] split() {
        int size = entries.size();
        int q = size/2;
        
        List<Entry<K, V>> left = entries.subList(0, q);
        List<Entry<K, V>> right = entries.subList(q, size);
        
        return null;
    }*/
    
    public Node<K, V> shadow() {
        if (getReferenceCount() == 1) {
            return this;
        }
        
        decrementReferenceCount();
        return clone();
    }
    
    @Override
    public Node<K, V> clone() {
        return new Node<K, V>(this);
    }
    
    public void writeTo(OutputStream out) throws IOException {
        
    }
    
    public static <K, V> Node<K, V> valueOf(InputStream in) throws IOException {
        return null;
    }
    
    public static class Id {

        public static Id create() {
            return new Id();
        }
    }
}
