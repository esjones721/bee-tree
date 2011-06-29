package org.ardverk.btree;

public interface TupleBinding<K, V> {

    public byte[] objectToKey(K key);
    
    public K keyToObject(byte[] key);
    
    public byte[] objectToValue(V value);
    
    public V valueToObject(byte[] value);
}
