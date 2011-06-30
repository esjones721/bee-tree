package org.ardverk.btree;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.ardverk.btree.Node.TupleNode;
import org.ardverk.btree.NodeProvider.Intent;

public class RootNode implements Lockable {

    private final ReentrantLock lock = new ReentrantLock();
    
    private final NodeProvider provider;
    
    private volatile Node root;
    
    private volatile int size = 0;
    
    public RootNode(NodeProvider provider, Node root, int size) {
        this.provider = provider;
        this.root = root;
        this.size = size;
    }
    
    @Override
    public void lock() {
        lock.lock();
    }
    
    @Override
    public void unlock() {
        lock.unlock();
    }
    
    public Node getRoot() {
        return root;
    }
    
    public Tuple put(byte[] key, byte[] value) {
        boolean success = false;
        lock();
        try {
            if (root.isOverflow()) {
                TupleNode median = root.split(provider);
                
                int height = root.getHeight() + 1;
                Node tmp = provider.allocate(height);
                
                tmp.addFirstNode(root.getId());
                tmp.addTupleNode(median);
                
                root = tmp;
            }
            
            root.lock();
            try {
                Tuple tuple = root.put(provider, this, key, value);
                
                // A new Key-Value was inserted!
                if (tuple == null) {
                    ++size;
                }
                success = true;
                return tuple;
            } finally {
                if (!success) {
                    root.unlock();
                }
            }
        } finally {
            if (!success) {
                unlock();
            }
        }
    }
    
    public synchronized Tuple remove(byte[] key) {
        Tuple tuple = root.remove(provider, key);
        
        if (!root.isLeaf() && root.isEmpty()) {
            Node tmp = root.firstChildNode(
                    provider, Intent.READ);
            provider.free(root);
            root = tmp;
        }
        
        // A Key-Value was removed!
        if (tuple != null) {
            --size;
        }
        
        return tuple;
    }
    
    public synchronized void clear() {
        size = 0;
        
        Node tmp = root;
        root = provider.allocate(0);
        
        provider.free(tmp);
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

    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }

    public Iterator<Tuple> iterator() {
        return root.iterator(provider);
    }
    
    public Iterator<Tuple> iterator(byte[] key, boolean inclusive) {
        return root.iterator(provider, key, inclusive);
    }
    
    @Override
    public String toString() {
        return "ROOT: " + root;
    }
}
