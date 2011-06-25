package org.ardverk.btree;

import java.util.Comparator;

import org.ardverk.btree.NodeProvider.Intent;

public class Node<K, V> {
    
    private final NodeId nodeId;
    
    private final int t;
    
    private final Bucket<Entry<K, V>> entries;
    
    private final Bucket<NodeId> nodes;
    
    public Node(boolean leaf, NodeId nodeId, int t) {
        this.nodeId = nodeId;
        this.t = t;
        
        entries = new Bucket<Entry<K, V>>(2*t-1);
        
        Bucket<NodeId> nodes = null;
        
        if (!leaf) {
            nodes = new Bucket<NodeId>(2*t);
        }
        
        this.nodes = nodes;
    }
    
    public NodeId getNodeId() {
        return nodeId;
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getEntryCount() {
        return entries.size();
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    public boolean isOverflow() {
        return entries.isOverflow();
    }
    
    public boolean isUnderflow() {
        return entries.size() < t;
    }
    
    public boolean isLeaf() {
        return nodes == null;
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
    
    private Entry<K, V> firstEntry() {
        return entries.getFirst();
    }
    
    private Entry<K, V> removeFirstEntry() {
        return entries.removeFirst();
    }
    
    private Entry<K, V> lastEntry() {
        return entries.getLast();
    }
    
    private Entry<K, V> removeLastEntry() {
        return entries.removeLast();
    }
    
    private NodeId firstNodeId() {
        return nodes.getFirst();
    }
    
    private NodeId removeFirstNodeId() {
        return nodes.removeFirst();
    }
    
    private NodeId lastNodeId() {
        return nodes.getLast();
    }
    
    private NodeId removeLastNodeId() {
        return nodes.removeLast();
    }
    
    public Entry<K, V> firstEntry(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstNode(provider, intent);
        }
        
        return node.firstEntry();
    }
    
    public Entry<K, V> lastEntry(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastNode(provider, intent);
        }
        
        return node.lastEntry();
    }
    
    Node<K, V> firstNode(NodeProvider<K, V> provider, Intent intent) {
        NodeId first = firstNodeId();
        return provider.get(first, intent);
    }
    
    private Node<K, V> lastNode(NodeProvider<K, V> provider, Intent intent) {
        NodeId last = lastNodeId();
        return provider.get(last, intent);
    }
    
    private NodeId getNodeId(int index) {
        return nodes.get(index);
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, 
            int index, Intent intent) {
        NodeId nodeId = getNodeId(index);
        return provider.get(nodeId, intent);
    }
    
    private Entry<K, V> setEntry(int index, Entry<K, V> entry) {
        return entries.set(index, entry);
    }
    
    public void addLastEntry(Entry<K, V> entry) {
        entries.addLast(entry);
    }
    
    public void addFirstEntry(Entry<K, V> entry) {
        entries.addFirst(entry);
    }
    
    private void addEntry(int index, Entry<K, V> entry) {
        entries.add(index, entry);
    }
    
    public void addLastNodeId(NodeId nodeId) {
        nodes.addLast(nodeId);
    }
    
    public void addFirstNodeId(NodeId nodeId) {
        nodes.addFirst(nodeId);
    }
    
    private void addNodeId(int index, NodeId nodeId) {
        nodes.add(index, nodeId);
    }
    
    public void addMedian(Median<K, V> median) {
        addMedian(getEntryCount(), median);
    }
    
    private void addMedian(int index, Median<K, V> median) {
        addEntry(index, median.getEntry());
        addNodeId(index+1, median.getNodeId());
    }
    
    private int binarySearch(NodeProvider<K, V> provider, K key) {
        Comparator<? super K> comparator = provider.comparator();
        return EntryUtils.binarySearch(entries, key, comparator);
    }
    
    public Entry<K, V> ceilingEntry(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        if (index >= 0 || isLeaf()) {
            if (index < 0) {
                index = -index - 1;
            }
            
            return getEntry(index);
        }
        
        Node<K, V> node = getNode(provider, -index - 1, Intent.READ);
        return node.ceilingEntry(provider, key);
    }
    
    public Entry<K, V> get(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        // Found the Key?
        if (index >= 0) {
            return getEntry(index);
        }
        
        // I didn't find it but I know where to look for it!
        if (!isLeaf()) {
            Node<K, V> node = getNode(provider, -index - 1, Intent.READ);
            return node.get(provider, key);
        }
        return null;
    }
    
    public Entry<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        int index = binarySearch(provider, key);
        
        // Replace an existing Key-Value
        if (index >= 0) {
            return setEntry(index, new Entry<K, V>(key, value));
        }
        
        assert (index < 0);
        index = -index - 1;
        
        // Found a leaf where it should be stored!
        if (isLeaf()) {
            assert (!isOverflow());
            addEntry(index, new Entry<K, V>(key, value));
            return null;
        }
        
        // Keep looking!
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isOverflow()) {
            
            Median<K, V> median = node.split(provider);
            addMedian(index, median);
            
            Comparator<? super K> comparator = provider.comparator();
            int cmp = comparator.compare(key, median.getKey());
            if (0 < cmp) {
                node = getNode(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public Entry<K, V> remove(NodeProvider<K, V> provider, K key) {
        
        int index = binarySearch(provider, key);
        
        // It must be here if it's a leaf!
        if (isLeaf()) {
            if (index >= 0) {
                return removeEntry(index);
            }
            return null;
        }
        
        // Found the Key-Value in an internal Node!
        if (index >= 0) {
            return removeInternal(provider, key, index);
        } 
        
        // Keep looking
        assert (index < 0);
        index = -index - 1;
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {
            fix(provider, node, index);
        }
        
        return node.remove(provider, key);
    }
    
    private Entry<K, V> removeInternal(NodeProvider<K, V> provider, K key, int index) {
        Entry<K, V> entry = null;
        
        Node<K, V> left = getNode(provider, index, Intent.WRITE);
        if (!left.isUnderflow()) {
            Entry<K, V> last = left.lastEntry(provider, Intent.WRITE);
            entry = setEntry(index, last);
            left.remove(provider, last.getKey());
            
        } else {
            
            Node<K, V> right = getNode(provider, index+1, Intent.WRITE);
            if (!right.isUnderflow()) {
                Entry<K, V> first = right.firstEntry(provider, Intent.WRITE);
                entry = setEntry(index, first);
                right.remove(provider, first.getKey());
            } else {
                
                Entry<K, V> median = removeEntry(index);
                removeNodeId(index+1);
                
                left.mergeWithRight(median, right);
                
                provider.free(right);
                
                entry = left.remove(provider, key);
            }
        }
        
        return entry;
    }
    
    private void fix(NodeProvider<K, V> provider, Node<K, V> node, int index) {
        
        Node<K, V> left = null;
        if (0 < index) {
            left = getNode(provider, index-1, Intent.WRITE);
        }
        
        // Borrow Entry from left sibling
        if (left != null && !left.isUnderflow()) {
            Entry<K, V> last = left.removeLastEntry();
            Entry<K, V> entry = setEntry(index-1, last);
            node.addFirstEntry(entry);
            
            if (!node.isLeaf()) {
                node.addFirstNodeId(left.removeLastNodeId());
            }
        } else {
            
            Node<K, V> right = null;
            if (index < getNodeCount()-1) {
                right = getNode(provider, index+1, Intent.WRITE);
            }
            
            // Borrow entry from right sibling
            if (right != null && !right.isUnderflow()) {
                Entry<K, V> first = right.removeFirstEntry();
                Entry<K, V> entry = setEntry(index, first);
                node.addLastEntry(entry);
                
                if (!node.isLeaf()) {
                    node.addLastNodeId(right.removeFirstNodeId());
                }
            
            // Neither sibling has enough Entries! Merge them!
            } else {
                
                if (left != null && !left.isEmpty()) {
                    Entry<K, V> median = removeEntry(index-1);
                    removeNodeId(index-1);
                    
                    node.mergeWithLeft(median, left);
                    
                    provider.free(left);
                } else {
                    
                    Entry<K, V> median = removeEntry(index);
                    removeNodeId(index);
                    
                    node.mergeWithRight(median, right);
                    
                    provider.free(right);
                }
            }
        }
    }
    
    private void mergeWithRight(Entry<K, V> median, Node<K, V> right) {
        entries.addLast(median);
        entries.addAll(right.entries);
        if (!isLeaf()) {
            nodes.addAll(right.nodes);
        }
    }
    
    private void mergeWithLeft(Entry<K, V> median, Node<K, V> left) {
        entries.addFirst(median);
        entries.addAll(0, left.entries);
        if (!isLeaf()) {
            nodes.addAll(0, left.nodes);
        }
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int entryCount = getEntryCount();
        int m = entryCount/2;
        
        Node<K, V> dst = provider.allocate(leaf);
        
        Entry<K, V> median = removeEntry(m);
        
        if (!leaf) {
            NodeId nodeId = removeNodeId(m+1);
            dst.addLastNodeId(nodeId);
        }
        
        while (m < getEntryCount()) {
            dst.addLastEntry(removeEntry(m));
            
            if (!leaf) {
                dst.addLastNodeId(removeNodeId(m+1));
            }
        }
        
        return new Median<K, V>(median, dst.getNodeId());
    }
    
    public Node<K, V> copy(NodeProvider<K, V> provider) {
        boolean leaf = isLeaf();
        Node<K, V> dst = provider.allocate(leaf);
        
        dst.entries.addAll(entries);
        
        if (!leaf) {
            dst.nodes.addAll(nodes);
        }
        
        return dst;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(entries);
        
        if (nodes != null) {
            sb.append(nodes);
        } else {
            sb.append("[]");
        }
        
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
        
        @Override
        public String toString() {
            return "<" + entry + ", " + nodeId + ">";
        }
    }
}
