package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ardverk.btree.Node.TupleNode;
import org.ardverk.btree.NodeProvider.Intent;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final Binding<K, V> binding;
    
    private final NodeProvider provider;
    
    public BeeTree(Binding<K, V> binding, NodeProvider provider) {
        this.binding = binding;
        this.provider = provider;
    }

    @Override
    public V put(K key, V value) {
        
        byte[] bk = binding.objectToKey(key);
        byte[] bv = binding.objectToValue(value);
        
        Tuple existing = null;
        synchronized (provider) {
            Node root = provider.getRoot();
            if (root.isOverflow()) {
                TupleNode median = root.split(provider);
                
                int height = root.getHeight() + 1;
                Node tmp = provider.allocate(height);
                
                tmp.addFirstNode(root.getId());
                tmp.addTupleNode(median);
                
                root = tmp;
                provider.setRoot(root);
            }
            
            existing = root.put(provider, bk, bv);
        }
        
        return existing != null ? binding.valueToObject(existing.getValue()) : null;
    }

    @Override
    public V remove(K key) {
        byte[] bk = binding.objectToKey(key);
        
        Tuple tuple = null;
        synchronized (provider) {
            Node root = provider.getRoot();
            tuple = root.remove(provider, bk);
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node tmp = root.firstChildNode(
                        provider, Intent.READ);
                provider.free(root);
                root = tmp;
                provider.setRoot(root);
            }
        }
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }

    @Override
    public void clear() {
        synchronized (provider) {
            provider.free(provider.getRoot());
            provider.setRoot(provider.allocate(0));
        }
    }
    
    @Override
    public V get(K key) {
        Tuple tuple = provider.getRoot().get(provider, 
                binding.objectToKey(key));
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }

    @Override
    public boolean contains(K key) {
        Tuple tuple = provider.getRoot().get(provider, 
                binding.objectToKey(key));
        return tuple != null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        Tuple tuple = provider.getRoot().ceilingTuple(provider, 
                binding.objectToKey(key));
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        Tuple tuple = provider.getRoot().firstTuple(provider, Intent.READ);
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        Tuple tuple = provider.getRoot().lastTuple(provider, Intent.READ);
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public boolean isEmpty() {
        return provider.getRoot().isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Iterator<Tuple> it = provider.getRoot().iterator(provider);
        return new EntryIterator(it);
    }
    
    @Override
    public Iterator<Entry<K, V>> iterator(K key, boolean inclusive) {
        Iterator<Tuple> it = provider.getRoot().iterator(provider, 
                binding.objectToKey(key), inclusive);
        return new EntryIterator(it);
    }
    
    private class EntryIterator implements Iterator<Entry<K, V>> {

        private final Iterator<Tuple> it;
        
        public EntryIterator(Iterator<Tuple> it) {
            this.it = it;
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return new TupleEntry(it.next());
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
    
    private class TupleEntry implements Map.Entry<K, V> {
        
        private final Tuple tuple;

        public TupleEntry(Tuple tuple) {
            this.tuple = tuple;
        }
        
        @Override
        public K getKey() {
            return binding.keyToObject(tuple.getKey());
        }
        
        @Override
        public V getValue() {
            return binding.valueToObject(tuple.getValue());
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}
