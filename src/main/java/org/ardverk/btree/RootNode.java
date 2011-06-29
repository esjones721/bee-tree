package org.ardverk.btree;

import java.util.Iterator;

import org.ardverk.btree.Node.TupleNode;
import org.ardverk.btree.NodeProvider.Intent;

public class RootNode {

    private final NodeProvider provider;
    
    private volatile Node root;
    
    public RootNode(NodeProvider provider, Node root) {
        this.provider = provider;
        this.root = root;
    }
    
    public synchronized Tuple put(byte[] key, byte[] value) {
        if (root.isOverflow()) {
            TupleNode median = root.split(provider);
            
            int height = root.getHeight() + 1;
            Node tmp = provider.allocate(height);
            
            tmp.addFirstNode(root.getId());
            tmp.addTupleNode(median);
            
            root = tmp;
        }
        
        return root.put(provider, key, value);
    }
    
    public synchronized Tuple remove(byte[] key) {
        Tuple tuple = root.remove(provider, key);
        
        if (!root.isLeaf() && root.isEmpty()) {
            Node tmp = root.firstChildNode(
                    provider, Intent.READ);
            provider.free(root);
            root = tmp;
        }
        
        return tuple;
    }
    
    public synchronized void clear() {
        provider.free(root);
        root = provider.allocate(0);
    }
    
    public Tuple get(byte[] key) {
        return root.get(provider, key);
    }
    
    public Tuple ceilingTuple(byte[] key) {
        return root.ceilingTuple(provider, key);
    }
    
    public Tuple firstTuple() {
        return root.firstTuple(provider, Intent.READ);
    }

    public Tuple lastTuple() {
        return root.lastTuple(provider, Intent.READ);
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public Iterator<Tuple> iterator() {
        return root.iterator(provider);
    }
    
    public Iterator<Tuple> iterator(byte[] key, boolean inclusive) {
        return root.iterator(provider, key, inclusive);
    }
}
