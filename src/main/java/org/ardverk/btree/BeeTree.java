package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map.Entry;

import org.ardverk.btree.fs.TupleBinding2;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final TupleBinding2<K, V> binding;
    
    private final NodeProvider<byte[], byte[]> provider;
    
    public BeeTree(BeeTreeNodeProvider provider) {
        this.provider = provider;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return provider.getRoot().iterator(provider);
    }

    @Override
    public V put(K key, V value) {
        synchronized (provider) {
            
        }
        return null;
    }

    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V get(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(K key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isEmpty() {
        return provider.getRoot().isEmpty();
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K key, boolean inclusive) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private class EntryIterator implements Iterator<Entry<K, V>> {

        private final Iterator<Tuple<byte[], byte[]>> it;
        
        public EntryIterator(Iterator<Tuple<byte[], byte[]>> it) {
            this.it = it;
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return new Map.Entry<K, V>() {
            };
        }

        @Override
        public void remove() {
            it.remove();
        }
        
    }
}
