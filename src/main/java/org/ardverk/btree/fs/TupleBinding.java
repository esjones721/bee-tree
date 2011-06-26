package org.ardverk.btree.fs;

import java.util.Map;

import org.ardverk.btree.Tuple;

public class TupleBinding<K, V> {

    private final EntryBinding<K> kb;
    
    private final EntryBinding<V> vb;

    public TupleBinding(EntryBinding<K> kb, EntryBinding<V> vb) {
        this.kb = kb;
        this.vb = vb;
    }
    
    public K entryToKey(byte[] key) {
        return kb.entryToObject(key);
    }
    
    public byte[] objectToKey(K key) {
        return kb.objectToEntry(key);
    }
    
    public V entryToValue(byte[] value) {
        return vb.entryToObject(value);
    }
    
    public byte[] objectToValue(V value) {
        return vb.objectToEntry(value);
    }
    
    public Map.Entry<K, V> tupleToEntry(Tuple<byte[], byte[]> tuple) {
        return tupleToEntry(tuple.getKey(), tuple.getValue());
    }
    
    public Map.Entry<K, V> tupleToEntry(final byte[] k, final byte[] v) {
        return new Map.Entry<K, V>() {

            private final K key = entryToKey(k);
            
            private final V value = entryToValue(v);
            
            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public String toString() {
                return key + "=" + value;
            }
        };
    }
}
