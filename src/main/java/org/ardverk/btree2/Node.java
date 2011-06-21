package org.ardverk.btree2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree2.NodeProvider.Intent;

public class Node<K, V> {

    private static final int t = 4;
    
    private final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(2*t-1);
    
    private final List<Id> nodes = new ArrayList<Id>(2*t);
    
    private final Id nodeId;
    
    private final boolean leaf;
    
    public Node(boolean leaf) {
        this(new Id(), leaf);
    }
    
    public Node(Id nodeId, boolean leaf) {
        this.nodeId = nodeId;
        this.leaf = leaf;
    }
    
    public Id getId() {
        return nodeId;
    }
    
    public int size() {
        return entries.size();
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    public boolean isFull() {
        return size() >= 2*t-1;
    }
    
    public void add(Entry<K, V> entry) {
        entries.add(entry);
    }
    
    public void add(Id nodeId) {
        nodes.add(nodeId);
    }
    
    public Entry<K, V> get(NodeProvider<K, V> provider, K key) {
        if (!isEmpty()) {
            Comparator<? super K> comparator = provider.comparator();
            int index = EntryUtils.binarySearch(entries, key, comparator);
            
            // Found the Key?
            if (index >= 0) {
                return entries.get(index);
            }
            
            // Didn't find it but know where to look for it!
            if (!leaf) {
                return get(provider, key, -index - 1, Intent.READ);
            }
        }
        return null;
    }
    
    private Node<K, V> get(NodeProvider<K, V> provider, int index, Intent intent) {
        Id nodeId = nodes.get(index);
        return provider.get(nodeId, intent);
    }
    
    private Entry<K, V> get(NodeProvider<K, V> provider, K key, int index, Intent intent) {
        Node<K, V> node = get(provider, index, intent);
        return node.get(provider, key);
    }
    
    public Entry<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        Comparator<? super K> comparator = provider.comparator();
        int index = EntryUtils.binarySearch(entries, key, comparator);
        
        if (index >= 0) {
            return entries.set(index, new Entry<K, V>(key, value));
        }
        
        assert (index < 0);
        index = -index - 1;
        
        if (leaf) {
            assert (!isFull());
            
            entries.add(index, new Entry<K, V>(key, value));
            return null;
        }
        
        Node<K, V> node = get(provider, index, Intent.WRITE);
        
        if (node.isFull()) {
            Split<K, V> split = node.split(provider);
            
            Entry<K, V> median = split.getMedian();
            entries.add(index, median);
            nodes.add(index+1, split.getNodeId());
            
            int cmp = comparator.compare(key, median.getKey());
            if (cmp > 0) {
                node = get(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public Split<K, V> split(NodeProvider<K, V> provider) {
        // I'm left!
        Node<K, V> right = provider.create(leaf);
        
        int size = entries.size();
        int m = size/2;
        
        //System.out.println("split().before: " + this);
        
        Entry<K, V> median = entries.remove(m);
        for (int i = 0; i < (size-m-1); i++) {
            right.entries.add(entries.remove(m));
            
            if (!leaf) {
                right.nodes.add(nodes.remove(m+1));
            }
        }
        
        if (!leaf) {
            right.nodes.add(nodes.remove(m+1));
        }
        
        //System.out.println("split().median: " + median);
        //System.out.println("split().left: " + this);
        //System.out.println("split().right: " + dst);
        
        return new Split<K, V>(median, right.getId());
    }
    
    @Override
    public String toString() {
        return nodeId + " @ " + entries + ", " + nodes;
    }
    
    public static class Split<K, V> {
        
        private final Entry<K, V> median;
        
        private final Id nodeId;

        public Split(Entry<K, V> median, Id nodeId) {
            this.median = median;
            this.nodeId = nodeId;
        }

        public Entry<K, V> getMedian() {
            return median;
        }

        public Id getNodeId() {
            return nodeId;
        }
    }
    
    public static class Id {
        
        private static final AtomicInteger COUNTER = new AtomicInteger();
        
        private final int value = COUNTER.incrementAndGet();
        
        @Override
        public int hashCode() {
            return value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Id)) {
                return false;
            }
            
            Id other = (Id)o;
            return value == other.value;
        }
        
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}