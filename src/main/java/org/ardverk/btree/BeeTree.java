package org.ardverk.btree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public interface BeeTree<K, V> extends Iterable<Map.Entry<K, V>> {

    public Comparator<? super K> comparator();
    
    public V put(K key, V value);
    
    public V get(K key);
    
    public V remove(K key);
    
    public boolean contains(K key);
    
    public int size();
    
    public boolean isEmpty();
    
    public Iterator<Map.Entry<K, V>> iterator(K key);
    
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive);
}
