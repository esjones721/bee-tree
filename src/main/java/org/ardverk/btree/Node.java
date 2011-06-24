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
    
    private Entry<K, V> firstEntry(boolean remove) {
        if (remove) {
            return entries.removeFirst();
        }
        return entries.getFirst();
    }
    
    private Entry<K, V> lastEntry(boolean remove) {
        if (remove) {
            return entries.removeLast();
        }
        return entries.getLast();
    }
    
    private NodeId firstNodeId(boolean remove) {
        if (remove) {
            return nodes.removeFirst();
        }
        return nodes.getFirst();
    }
    
    private NodeId lastNodeId(boolean remove) {
        if (remove) {
            return nodes.removeLast();
        }
        return nodes.getLast();
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
    
    public void addLast(Entry<K, V> entry) {
        entries.addLast(entry);
    }
    
    public void addFirst(Entry<K, V> entry) {
        entries.addFirst(entry);
    }
    
    private void add(int index, Entry<K, V> entry) {
        entries.add(index, entry);
    }
    
    public void addLast(NodeId nodeId) {
        nodes.addLast(nodeId);
    }
    
    public void addFirst(NodeId nodeId) {
        nodes.addFirst(nodeId);
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
    
    public Entry<K, V> ceilingEntry(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        if (index >= 0 || isLeaf()) {
            if (index < 0) {
                index = -index - 1;
            }
            
            return getEntry(index);
        }
        
        return getEntry(provider, key, -index - 1, Intent.READ);
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
            Entry<K, V> largest = left.findLargestEntry(provider);
            entry = set(index, largest);
            left.remove(provider, largest.getKey());
            
        } else {
            
            Node<K, V> right = getNode(provider, index+1, Intent.WRITE);
            if (!right.isUnderflow()) {
                Entry<K, V> smallest = right.findSmallestEntry(provider);
                entry = set(index, smallest);
                right.remove(provider, smallest.getKey());
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
            Entry<K, V> last = left.lastEntry(true);
            Entry<K, V> entry = set(index-1, last);
            node.addFirst(entry);
            
            if (!node.isLeaf()) {
                node.addFirst(left.lastNodeId(true));
            }
        } else {
            
            Node<K, V> right = null;
            if (index < getNodeCount()-1) {
                right = getNode(provider, index+1, Intent.WRITE);
            }
            
            // Borrow entry from right sibling
            if (right != null && !right.isUnderflow()) {
                Entry<K, V> first = right.firstEntry(true);
                Entry<K, V> entry = set(index, first);
                node.addLast(entry);
                
                if (!node.isLeaf()) {
                    node.addLast(right.firstNodeId(true));
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
    
    private Entry<K, V> findSmallestEntry(NodeProvider<K, V> provider) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstNode(provider, Intent.WRITE);
        }
        
        return node.firstEntry(false);
    }
    
    private Entry<K, V> findLargestEntry(NodeProvider<K, V> provider) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastNode(provider, Intent.WRITE);
        }
        
        return node.lastEntry(false);
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int entryCount = getEntryCount();
        int m = entryCount/2;
        
        Node<K, V> dst = provider.allocate(leaf);
        
        Entry<K, V> median = removeEntry(m);
        
        if (!leaf) {
            NodeId nodeId = removeNodeId(m+1);
            dst.addLast(nodeId);
        }
        
        while (m < getEntryCount()) {
            dst.addLast(removeEntry(m));
            
            if (!leaf) {
                dst.addLast(removeNodeId(m+1));
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
