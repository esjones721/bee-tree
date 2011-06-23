package org.ardverk.btree3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree3.NodeProvider.Intent;

class Node<K, V> {
    
    private static final int t = 256;
    
    private final Id nodeId = new Id();
    
    private final List<Entry<K, V>> entries 
        = new ArrayList<Entry<K, V>>(16);
    
    public Node() {
        this(null);
    }
    
    public Node(Id childId) {
        // A B-Tree has n-keys and n+1 children! This is dummy Entry that 
        // is always stored at the front of the List and doesn't hold anything 
        // but the additional child node. Reason: We can use a single Array.
        entries.add(new Entry<K, V>(null, null, childId));
    }
    
    public Id getId() {
        return nodeId;
    }
    
    public int size() {
        return entries.size() - 1;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public boolean isFull() {
        return size() >= 2*t-1;
    }
    
    public boolean isLeaf() {
        Entry<K, V> entry = entries.get(0);
        return entry.getNodeId() == null;
    }
    
    private Entry<K, V> getEntry(int index) {
        return entries.get(1 + index);
    }
    
    private Entry<K, V> removeEntry(int index) {
        return entries.remove(1 + index);
    }
    
    private Entry<K, V> firstEntry(boolean remove) {
        if (!isEmpty()) {
            if (remove) {
                return removeEntry(0);
            }
            return getEntry(0);
        }
        return null;
    }
    
    private Entry<K, V> lastEntry(boolean remove) {
        if (!isEmpty()) {
            if (remove) {
                return removeEntry(size()-1);
            }
            return getEntry(size()-1);
        }
        return null;
    }
    
    private Id getNodeId(int index) {
        Entry<K, V> entry = entries.get(index);
        return entry.getNodeId();
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, 
            int index, Intent intent) {
        Id nodeId = getNodeId(index);
        return provider.get(nodeId, intent);
    }
    
    private Entry<K, V> getEntry(NodeProvider<K, V> provider, 
            K key, int index, Intent intent) {
        Node<K, V> node = getNode(provider, index, intent);
        return node.get(provider, key);
    }
    
    private Entry<K, V> set(int index, K key, V value) {
        Entry<K, V> existing = entries.get(index + 1);
        entries.set(index + 1, new Entry<K, V>(key, value, existing));
        return existing;
    }
    
    public void add(Entry<K, V> entry) {
        add(size(), entry);
    }
    
    private void add(NodeProvider<K, V> provider, Entry<K, V> entry) {
        int index = binarySearch(provider, entry.getKey());
        if (index < 0) {
            index = -index - 1;
        }
        
        add(index, entry);
    }
    
    private void add(int index, Entry<K, V> entry) {
        entries.add(index + 1, entry);
    }
    
    private int binarySearch(NodeProvider<K, V> provider, K key) {
        Comparator<? super K> comparator = provider.comparator();
        return EntryUtils.binarySearch(entries.subList(1, entries.size()), key, comparator);
    }
    
    public Entry<K, V> get(NodeProvider<K, V> provider, K key) {
        if (!isEmpty()) {
            int index = binarySearch(provider, key);
            
            // Found the Key?
            if (index >= 0) {
                return getEntry(index);
            }
            
            // Didn't find it but know where to look for it!
            if (!isLeaf()) {
                return getEntry(provider, key, -index - 1, Intent.READ);
            }
        }
        return null;
    }
    
    public Entry<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        int index = binarySearch(provider, key);
        
        if (index >= 0) {
            return set(index, key, value);
        }
        
        assert (index < 0);
        index = -index - 1;
        
        if (isLeaf()) {
            assert (!isFull());
            add(index, new Entry<K, V>(key, value));
            return null;
        }
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isFull()) {
            
            Entry<K, V> entry = node.split(provider);
            add(index, entry);
            
            Comparator<? super K> comparator = provider.comparator();
            int cmp = comparator.compare(key, entry.getKey());
            if (0 < cmp) {
                node = getNode(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public Entry<K, V> split(NodeProvider<K, V> provider) {
        
        int size = size();
        int m = size/2;
        
        Entry<K, V> median = removeEntry(m);
        
        Node<K, V> dst = provider.create(median.getNodeId());
        
        while (m < size()) {
            dst.add(removeEntry(m));
        }
        
        return new Entry<K, V>(median, dst.getId());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        
        if (!isEmpty()) {
            boolean first = true;
            for (Entry<K, V> entry : entries) {
                if (first) {
                    first = false;
                    continue;
                }
                
                sb.append(entry.getKey()).append("=")
                    .append(entry.getValue()).append(", ");
            }
            sb.setLength(sb.length()-2);
        }
        
        sb.append("] [");
        
        if (!entries.isEmpty()) {
            for (Entry<K, V> entry : entries) {
                sb.append(entry.getNodeId()).append(", ");
            }
            sb.setLength(sb.length()-2);
        }
        
        sb.append("]");
        
        return sb.toString();
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
