package org.ardverk.btree2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ardverk.btree2.NodeProvider.Intent;

public class Node<K, V> {

    private static final int t = 4;
    
    private final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
    
    private final List<Id> nodes = new ArrayList<Id>();
    
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
        return nodes.size();
    }
    
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    public boolean isFull() {
        return size() >= 2*t-1;
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
                return get(provider, key, -index - 1);
            }
        }
        return null;
    }
    
    private Entry<K, V> get(NodeProvider<K, V> provider, K key, int index) {
        Entry<K, V> entry = entries.get(index);
        
        Comparator<? super K> comparator = provider.comparator();
        int diff = comparator.compare(key, entry.getKey());
        
        Id nodeId = null;
        if (diff < 0) {
            nodeId = nodes.get(index);
        } else {
            nodeId = nodes.get(index + 1);
        }
        
        Node<K, V> node = provider.get(nodeId, Intent.READ);
        return node.get(provider, key);
    }
    
    public static class Id {
        
    }
}
