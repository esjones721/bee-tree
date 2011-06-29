package org.ardverk.btree.fs;

public interface TupleBinding2<K, V> {

    public byte[] objectToKey(K key);
    
    public byte[] objectToValue(V value);
    
    public K keyToObject(byte[] key);
    
    public V valueToObject(byte[] value);
}
