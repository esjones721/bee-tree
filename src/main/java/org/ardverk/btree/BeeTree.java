package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final NodeProvider provider;
    
    private final TupleBinding<K, V> binding;
    
    public BeeTree(NodeProvider provider, TupleBinding<K, V> binding) {
        this.binding = binding;
        this.provider = provider;
    }

    @Override
    public V put(K key, V value) {
        Tuple tuple = provider.getRoot().put(
                binding.objectToKey(key), binding.objectToValue(value));
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }

    @Override
    public V remove(K key) {
        Tuple tuple = provider.getRoot().remove(binding.objectToKey(key));
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }

    @Override
    public void clear() {
        provider.getRoot().clear();
    }
    
    @Override
    public V get(K key) {
        Tuple tuple = provider.getRoot().get(binding.objectToKey(key));
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }

    @Override
    public boolean contains(K key) {
        Tuple tuple = provider.getRoot().get(binding.objectToKey(key));
        return tuple != null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        Tuple tuple = provider.getRoot().ceilingTuple(
                binding.objectToKey(key));
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        Tuple tuple = provider.getRoot().firstTuple();
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        Tuple tuple = provider.getRoot().lastTuple();
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public boolean isEmpty() {
        return provider.getRoot().isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Iterator<Tuple> it = provider.getRoot().iterator();
        return new EntryIterator(it);
    }
    
    @Override
    public Iterator<Entry<K, V>> iterator(K key, boolean inclusive) {
        Iterator<Tuple> it = provider.getRoot().iterator( 
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
