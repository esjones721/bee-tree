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
    
    public boolean isOverflow() {
        return getEntryCount() >= 2*t-1;
    }
    
    public boolean isUnderflow() {
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
    
    private Id removeNodeId(int index) {
        return nodes.remove(index);
    }
    
    private Entry<K, V> firstEntry(boolean remove) {
        if (remove) {
            return removeEntry(0);
        }
        return getEntry(0);
    }
    
    private Entry<K, V> lastEntry(boolean remove) {
        if (remove) {
            return removeEntry(getEntryCount()-1);
        }
        return getEntry(getEntryCount()-1);
    }
    
    private Id firstNodeId(boolean remove) {
        if (remove) {
            return removeNodeId(0);
        }
        return getNodeId(0);
    }
    
    private Id lastNodeId(boolean remove) {
        if (remove) {
            return removeNodeId(getNodeCount()-1);
        }
        return getNodeId(getNodeCount()-1);
    }
    
    Node<K, V> firstNode(NodeProvider<K, V> provider, Intent intent) {
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
    
    private Entry<K, V> set(int index, Entry<K, V> entry) {
        return entries.set(index, entry);
    }
    
    private Id set(int index, Id nodeId) {
        return nodes.set(index, nodeId);
    }
    
    /*private void add(NodeProvider<K, V> provider, Entry<K, V> entry) {
        int index = binarySearch(provider, entry.getKey());
        if (index < 0) {
            index = -index - 1;
        }
        
        add(index, entry);
    }*/
    
    public void add(Entry<K, V> entry) {
        entries.add(entry);
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
    
    public void add(Median<K, V> median) {
        add(getEntryCount(), median);
    }
    
    private void add(int index, Median<K, V> median) {
        add(index, median.getEntry());
        add(index+1, median.getNodeId());
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
        
        // I didn't find it but I know where to look for it!
        if (!isLeaf()) {
            return getEntry(provider, key, -index - 1, Intent.READ);
        }
        return null;
    }
    
    public Entry<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        int index = binarySearch(provider, key);
        
        // Replace an existing Key-Value
        if (index >= 0) {
            return set(index, new Entry<K, V>(key, value));
        }
        
        assert (index < 0);
        index = -index - 1;
        
        // Found a leaf where it should be stored!
        if (isLeaf()) {
            assert (!isOverflow());
            add(index, new Entry<K, V>(key, value));
            return null;
        }
        
        // Keep looking!
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isOverflow()) {
            
            Median<K, V> median = node.split(provider);
            add(index, median);
            
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
        
        // It must be here if it's a leaf!
        if (isLeaf()) {
            Entry<K, V> entry = null;
            if (index >= 0) {
                entry = removeEntry(index);
            }
            return;
        }
        
        // Found the Key-Value in an internal Node!
        if (index >= 0) {
            Node<K, V> y = leftChild(provider, index, Intent.WRITE);
            if (!y.isUnderflow()) {
                Entry<K, V> entry = y.largestEntry(provider, Intent.WRITE);
                set(index, entry);
                y.remove(provider, entry.getKey());System.out.println("A");
            } else {
                Node<K, V> z = rightChild(provider, index, Intent.WRITE);
                if (!z.isUnderflow()) {
                    Entry<K, V> entry = z.smallestEntry(provider, Intent.WRITE);
                    set(index, entry);
                    z.remove(provider, entry.getKey());System.out.println("B");
                } else {
                    // TODO: Not sure and delete Z!
                    y.merge(provider, this, index, z);
                    y.remove(provider, key);System.out.println("C");
                }
            }
            
            return;
        } 
        
        // Keep looking
        assert (index < 0);
        index = -index - 1;
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {
            if (index == 0) {
                Node<K, V> z = getNode(provider, index+1, Intent.WRITE);
                if (!z.isUnderflow()) {
                    
                    Entry<K, V> k1 = getEntry(index);
                    node.add(k1);
                    set(index+1, z.firstEntry(true));
                    node.add(z.firstNodeId(true));
                    
                } else if (index == getNodeCount()-1) {
                    z = getNode(provider, index-1, Intent.WRITE);
                    if (!z.isUnderflow()) {
                        Entry<K, V> k1 = getEntry(index);
                        node.add(0, k1);
                        set(index-1, z.firstEntry(true));
                        node.add(0, z.firstNodeId(true));
                        
                    } else {
                        // MERGE
                        node.merge(provider, this, index, z);
                    }
                } else {
                    // MERGE
                    node.merge(provider, this, index, z);
                }
            } else if (index == getNodeCount()-1) {
                Node<K, V> z = getNode(provider, index-1, Intent.WRITE);
                if (!z.isUnderflow()) {
                    Entry<K, V> k1 = getEntry(index);
                    node.add(0, k1);
                    set(index-1, z.firstEntry(true));
                    node.add(0, z.firstNodeId(true));
                } else {
                    // MERGE
                    node.merge(provider, this, index, z);
                }
            } else {
                // MERGE
            }
            
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
    
    private void merge(NodeProvider<K, V> provider, Node<K, V> parent, 
            int index, Node<K, V> src) {
        System.out.println(parent + " @ " + index);
        
        Entry<K, V> entry = parent.removeEntry(index-1);
        parent.removeNodeId(index);
        
        entries.add(entry);
        
        entries.addAll(src.entries);
        nodes.addAll(src.nodes);
        
        provider.free(src);
    }
    
    private Node<K, V> leftChild(NodeProvider<K, V> provider, int index, Intent intent) {
        return getNode(provider, index, intent);
    }
    
    private Node<K, V> rightChild(NodeProvider<K, V> provider, int index, Intent intent) {
        return getNode(provider, index+1, intent);
    }
    
    private Entry<K, V> smallestEntry(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstNode(provider, intent);
        }
        
        return node.firstEntry(false);
    }
    
    private Entry<K, V> largestEntry(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastNode(provider, intent);
        }
        
        return node.lastEntry(false);
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int entryCount = getEntryCount();
        int m = entryCount/2;
        
        Entry<K, V> median = removeEntry(m);
        
        Id nodeId = null;
        if (!leaf) {
            nodeId = removeNodeId(m+1);
        }
        
        Node<K, V> dst = provider.allocate(nodeId);
        
        while (m < getEntryCount()) {
            dst.add(removeEntry(m));
            
            if (!leaf) {
                dst.add(removeNodeId(m+1));
            }
        }
        
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
        
        private Median(Entry<K, V> entry, Id nodeId) {
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
