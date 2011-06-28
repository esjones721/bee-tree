package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final NodeProvider<K, V> provider;
    
    public BeeTree(NodeProvider<K, V> provider) {
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
}
