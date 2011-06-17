package org.ardverk.btree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.ardverk.btree.PageProvider.Intent;

class Page<K, V> implements Cloneable {

    private final AtomicLong timeStamp = new AtomicLong();
    
    private final AtomicInteger references = new AtomicInteger(1);
    
    private final boolean leaf;
    
    private final Page.Id pageId = Page.Id.create();
    
    private final NavigableMap<K, Node<K, V>> entries;
    
    private final int maxSize = 10;
    
    public Page(Comparator<? super K> comparator, boolean leaf) {
        this.leaf = leaf;
        
        entries = new TreeMap<K, Node<K,V>>(comparator);
    }
    
    public long getTimeStamp() {
        return timeStamp.get();
    }
    
    public void touch() {
        timeStamp.set(System.currentTimeMillis());
    }
    
    public boolean isDirty() {
        return getTimeStamp() != 0L;
    }
    
    public Id getPageId() {
        return pageId;
    }
    
    private boolean isFull() {
        return entries.size() >= maxSize;
    }
    
    private int referenceCount() {
        return references.get();
    }
    
    private int reference() {
        touch();
        return references.incrementAndGet();
    }
    
    private int dereference() {
        touch();
        return references.decrementAndGet();
    }
    
    public int size() {
        return entries.size();
    }
    
    private Node<K, V> ceilingNode(K key) {
        Map.Entry<K, Node<K, V>> entry = entries.ceilingEntry(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public V get(PageProvider<K, V> provider, K key) {
        Node<K, V> node = ceilingNode(key);
        
        if (node != null) {
            K other = node.getKey();
            if (key.equals(other)) {
                return node.getValue();
            }
            
            Page<K, V> child = provider.get(node, Intent.READ);
            return child.get(provider, key);
        }
        
        return null;
    }
    
    public V put(PageProvider<K, V> provider, K key, V value) {
        
        if (leaf) {
            if (!isFull()) {
                Node<K, V> element = new Node<K, V>(key, value);
                Node<K, V> existing = entries.put(key, element);
                
                return existing != null ? existing.getValue() : null;
            } else {
                
            }
        }
        
        Node<K, V> closest = ceilingNode(key);
        Page<K, V> child = provider.get(closest, Intent.WRITE);
        
        return child.put(provider, key, value);
    }
    
    /*public Node<K, V>[] split() {
        int size = entries.size();
        int q = size/2;
        
        List<Entry<K, V>> left = entries.subList(0, q);
        List<Entry<K, V>> right = entries.subList(q, size);
        
        return null;
    }*/
    
    public Page<K, V> shadow() {
        if (referenceCount() == 1) {
            return this;
        }
        
        dereference();
        return clone();
    }
    
    @Override
    public Page<K, V> clone() {
        //return new Page<K, V>(this);
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        
    }
    
    public static <K, V> Page<K, V> valueOf(InputStream in) throws IOException {
        return null;
    }
    
    public static class Id {

        public static Id create() {
            return new Id();
        }
    }
}
