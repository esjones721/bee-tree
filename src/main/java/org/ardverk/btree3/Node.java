package org.ardverk.btree3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree3.NodeProvider.Intent;

class Node<K, V> {
    
    private static final int t = 2;
    
    private final Id nodeId = new Id();
    
    private final List<Entry<K, V>> entries 
        = new ArrayList<Entry<K, V>>(2*t-1);
    
    private final List<Id> nodes 
        = new ArrayList<Id>(2*t);
    
    public Node(Id init) {
        if (init != null) {
            nodes.add(init);
        }
    }
    
    public Id getId() {
        return nodeId;
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getEntryCount() {
        return entries.size();
    }
    
    public boolean isEmpty() {
        return getEntryCount() == 0;
    }
    
    public boolean isFull() {
        return getEntryCount() >= 2*t-1;
    }
    
    private boolean isUnderflow() {
        return getEntryCount() < t;
    }
    
    public boolean isLeaf() {
        return nodes.isEmpty();
    }
    
    private Entry<K, V> getEntry(int index) {
        return entries.get(index);
    }
    
    private Entry<K, V> removeEntry(int index) {
        return entries.remove(index);
    }
    
    private Id removeNode(int index) {
        return nodes.remove(index);
    }
    
    private Entry<K, V> firstEntry() {
        return getEntry(0);
    }
    
    private Entry<K, V> lastEntry() {
        return getEntry(getEntryCount()-1);
    }
    
    private Node<K, V> firstNode(NodeProvider<K, V> provider, Intent intent) {
        return getNode(provider, 0, intent);
    }
    
    private Node<K, V> lastNode(NodeProvider<K, V> provider, Intent intent) {
        return getNode(provider, getNodeCount()-1, intent);
    }
    
    private Id getNodeId(int index) {
        return nodes.get(index);
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
        return set(index, new Entry<K, V>(key, value));
    }
    
    private Entry<K, V> set(int index, Entry<K, V> entry) {
        return entries.set(index, entry);
    }
    
    public void add(Entry<K, V> entry) {
        add(getEntryCount(), entry);
    }
    
    /*private void add(NodeProvider<K, V> provider, Entry<K, V> entry) {
        int index = binarySearch(provider, entry.getKey());
        if (index < 0) {
            index = -index - 1;
        }
        
        add(index, entry);
    }*/
    
    private void add(int index, K key, V value) {
        add(index, new Entry<K, V>(key, value));
    }
    
    private void add(int index, Entry<K, V> entry) {
        entries.add(index, entry);
    }
    
    public void add(Id nodeId) {
        nodes.add(nodeId);
    }
    
    private void add(int index, Id nodeId) {
        nodes.add(index, nodeId);
    }
    
    private int binarySearch(NodeProvider<K, V> provider, K key) {
        Comparator<? super K> comparator = provider.comparator();
        return EntryUtils.binarySearch(entries, key, comparator);
    }
    
    public Entry<K, V> get(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        // Found the Key?
        if (index >= 0) {
            return getEntry(index);
        }
        
        // Didn't find it but know where to look for it!
        if (!isLeaf()) {
            return getEntry(provider, key, -index - 1, Intent.READ);
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
            add(index, key, value);
            return null;
        }
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isFull()) {
            
            Median<K, V> median = node.split(provider);
            
            add(index, median.getEntry());
            add(index+1, median.getNodeId());
            
            Comparator<? super K> comparator = provider.comparator();
            int cmp = comparator.compare(key, median.getKey());
            if (0 < cmp) {
                node = getNode(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public void remove(NodeProvider<K, V> provider, K key) {
        
        int index = binarySearch(provider, key);
        
        if (isLeaf()) {
            if (index >= 0) {
                removeEntry(index);
            }
        } else {
            
            if (index >= 0) {
                Node<K, V> y = leftChild(provider, index);
                if (!y.isUnderflow()) {
                    Entry<K, V> entry = y.largestEntry(provider);
                    set(index, entry);
                    y.remove(provider, entry.getKey());System.out.println("A");
                } else {
                    Node<K, V> z = rightChild(provider, index);
                    if (!z.isUnderflow()) {
                        Entry<K, V> entry = z.smallestEntry(provider);
                        set(index, entry);
                        z.remove(provider, entry.getKey());System.out.println("B");
                    } else {
                        // TODO: Not sure and delete Z!
                        y.merge(this, index, z);
                        y.remove(provider, key);System.out.println("C");
                    }
                }
            } else {
                assert (index < 0);
                index = -index - 1;
                
                Node<K, V> node = getNode(provider, index, Intent.WRITE);
                
                if (node.isUnderflow()) {
                    if (index == getNodeCount()-1) {
                        Node<K, V> left = getNode(provider, index-1, Intent.WRITE);
                        if (!left.isUnderflow()) {
                            
                        } else {
                            
                        }
                    } else {
                        if (index == 0) {
                            Node<K, V> right = getNode(provider, 1, Intent.WRITE);
                            if (!right.isUnderflow()) {
                                
                            } else {
                                
                            }
                        }
                    }
                }
                
                node.remove(provider, key);
            }
        }
    }
    
    private void merge(Node<K, V> parent, int index, Node<K, V> src) {
        System.out.println(parent + " @ " + index);
        
        Entry<K, V> entry = parent.removeEntry(index-1);
        //Id nodeId = parent.removeNode(index);
        
        entries.add(entry);
        //src.add(nodeId);
        
        entries.addAll(src.entries);
        nodes.addAll(src.nodes);
        
        System.out.println("MERGED: " + this);
    }
    
    private Node<K, V> leftChild(NodeProvider<K, V> provider, int index) {
        return getNode(provider, index, Intent.WRITE);
    }
    
    private Node<K, V> rightChild(NodeProvider<K, V> provider, int index) {
        return getNode(provider, index+1, Intent.WRITE);
    }
    
    private Entry<K, V> largestEntry(NodeProvider<K, V> provider) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastNode(provider, Intent.WRITE);
        }
        
        return node.lastEntry();
    }
    
    private Entry<K, V> smallestEntry(NodeProvider<K, V> provider) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstNode(provider, Intent.WRITE);
        }
        
        return node.firstEntry();
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int entryCount = getEntryCount();
        int m = entryCount/2;
        
        Entry<K, V> median = removeEntry(m);
        
        Id nodeId = null;
        if (!leaf) {
            nodeId = removeNode(m+1);
        }
        
        Node<K, V> dst = provider.create(nodeId);
        
        //System.out.println("BEFORE: " + this);
        
        while (m < getEntryCount()) {
            dst.add(removeEntry(m));
            
            if (!leaf) {
                dst.add(removeNode(m+1));
            }
        }
        
        //System.out.println("AFTER.l" + this);
        //System.out.println("AFTER.r" + dst);
        //System.out.println("AFTER.m" + median);
        
        return new Median<K, V>(median, dst.getId());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(entries).append(nodes);
        return sb.toString();
    }
    
    public static class Median<K, V> {
        
        private final Entry<K, V> entry;
        
        private final Id nodeId;
        
        public Median(Entry<K, V> entry, Id nodeId) {
            this.entry = entry;
            this.nodeId = nodeId;
        }

        public K getKey() {
            return entry.getKey();
        }
        
        public Entry<K, V> getEntry() {
            return entry;
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
