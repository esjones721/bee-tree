package org.ardverk.btree3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ardverk.btree3.NodeProvider.Intent;

class Node<K, V> {
    
    private static final int t = 2;
    
    private final List<Entry<K, V>> entries 
        = new ArrayList<Entry<K, V>>(2*t-1);
    
    private final List<NodeId> nodes 
        = new ArrayList<NodeId>(2*t);
    
    private final NodeId nodeId;
    
    public Node(NodeId nodeId) {
        this.nodeId = nodeId;
    }
    
    public NodeId getId() {
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
    
    private NodeId removeNodeId(int index) {
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
    
    private NodeId firstNodeId(boolean remove) {
        if (remove) {
            return removeNodeId(0);
        }
        return getNodeId(0);
    }
    
    private NodeId lastNodeId(boolean remove) {
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
    
    private NodeId getNodeId(int index) {
        return nodes.get(index);
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, 
            int index, Intent intent) {
        NodeId nodeId = getNodeId(index);
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
    
    private NodeId set(int index, NodeId nodeId) {
        return nodes.set(index, nodeId);
    }
    
    /*private void add(NodeProvider<K, V> provider, Entry<K, V> entry) {
        int index = binarySearch(provider, entry.getKey());
        if (index < 0) {
            index = -index - 1;
        }
        
        add(index, entry);
    }*/
    
    public void addLast(Entry<K, V> entry) {
        entries.add(entry);
    }
    
    public void addFirst(Entry<K, V> entry) {
        add(0, entry);
    }
    
    private void add(int index, Entry<K, V> entry) {
        entries.add(index, entry);
    }
    
    public void addLast(NodeId nodeId) {
        nodes.add(nodeId);
    }
    
    public void addFirst(NodeId nodeId) {
        add(0, nodeId);
    }
    
    private void add(int index, NodeId nodeId) {
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
                y.remove(provider, entry.getKey());
            } else {
                Node<K, V> z = rightChild(provider, index, Intent.WRITE);
                if (!z.isUnderflow()) {
                    Entry<K, V> entry = z.smallestEntry(provider, Intent.WRITE);
                    set(index, entry);
                    z.remove(provider, entry.getKey());
                } else {
                    
                    Entry<K, V> entry = removeEntry(index);
                    removeNodeId(index+1);
                    
                    y.addLast(entry);
                    y.entries.addAll(z.entries);
                    
                    if (!y.isLeaf()) {
                        y.nodes.addAll(z.nodes);
                    }
                    
                    provider.free(z);
                    
                    y.remove(provider, key);
                }
            }
            
            return;
        } 
        
        // Keep looking
        assert (index < 0);
        index = -index - 1;
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {

            Node<K, V> ls = null;
            if (0 < index) {
                ls = getNode(provider, index-1, Intent.WRITE);
            }
            
            if (ls != null && !ls.isUnderflow()) {
                Entry<K, V> bla = ls.lastEntry(true);
                Entry<K, V> foo = set(index-1, bla);
                node.addFirst(foo);
                
                if (!node.isLeaf()) {
                    node.addFirst(ls.lastNodeId(true));
                }
            } else {
                
                Node<K, V> rs = null;
                if (index < getNodeCount()-1) {
                    rs = getNode(provider, index+1, Intent.WRITE);
                }
                
                if (rs != null && !rs.isUnderflow()) {
                    Entry<K, V> bla = rs.firstEntry(true);
                    Entry<K, V> foo = set(index, bla);
                    node.addLast(foo);
                    
                    if (!node.isLeaf()) {
                        node.addLast(rs.firstNodeId(true));
                    }
                
                } else {
                    
                    if (ls != null && !ls.isEmpty()) {
                        node.entries.addAll(0, ls.entries);
                        if (!node.isLeaf()) {
                            node.nodes.addAll(0, ls.nodes);
                        }
                        node.entries.add(t-1, removeEntry(index-1));
                        removeNodeId(index-1);
                        provider.free(ls);
                    } else {
                        node.entries.addAll(rs.entries);
                        if (!node.isLeaf()) {
                            node.nodes.addAll(rs.nodes);
                        }
                        
                        node.entries.add(t-1, removeEntry(index));
                        removeNodeId(index);
                        provider.free(rs);
                    }
                }
            }
        }
        
        node.remove(provider, key);
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
        
        NodeId nodeId = null;
        if (!leaf) {
            nodeId = removeNodeId(m+1);
        }
        
        Node<K, V> dst = provider.allocate(nodeId);
        
        while (m < getEntryCount()) {
            dst.addLast(removeEntry(m));
            
            if (!leaf) {
                dst.addLast(removeNodeId(m+1));
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
        
        private final NodeId nodeId;
        
        private Median(Entry<K, V> entry, NodeId nodeId) {
            this.entry = entry;
            this.nodeId = nodeId;
        }

        public K getKey() {
            return entry.getKey();
        }
        
        public Entry<K, V> getEntry() {
            return entry;
        }

        public NodeId getNodeId() {
            return nodeId;
        }
    }
}
