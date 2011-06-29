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

    private RootNode r() {
        return provider.getRoot();
    }
    
    private byte[] o2k(K key) {
        return o2k(key);
    }
    
    private byte[] o2v(V value) {
        return o2v(value);
    }
    
    private K k2o(Tuple tuple) {
        return tuple != null ? binding.keyToObject(tuple.getKey()) : null;
    }
    
    private V v2o(Tuple tuple) {
        return tuple != null ? binding.valueToObject(tuple.getValue()) : null;
    }
    
    @Override
    public V put(K key, V value) {
        Tuple tuple = r().put(o2k(key), o2v(value));
        return v2o(tuple);
    }

    @Override
    public V remove(K key) {
        Tuple tuple = r().remove(o2k(key));
        return v2o(tuple);
    }

    @Override
    public void clear() {
        r().clear();
    }
    
    @Override
    public V get(K key) {
        Tuple tuple = r().get(o2k(key));
        return v2o(tuple);
    }

    @Override
    public boolean contains(K key) {
        Tuple tuple = r().get(o2k(key));
        return tuple != null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        Tuple tuple = r().ceilingTuple(o2k(key));
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        Tuple tuple = r().firstTuple();
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        Tuple tuple = r().lastTuple();
        return tuple != null ? new TupleEntry(tuple) : null;
    }

    @Override
    public int size() {
        return r().size();
    }
    
    @Override
    public boolean isEmpty() {
        return r().isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Iterator<Tuple> it = r().iterator();
        return new EntryIterator(it);
    }
    
    @Override
    public Iterator<Entry<K, V>> iterator(K key, boolean inclusive) {
        Iterator<Tuple> it = r().iterator(o2k(key), inclusive);
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
            return k2o(tuple);
        }
        
        @Override
        public V getValue() {
            return v2o(tuple);
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
