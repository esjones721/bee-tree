package org.ardverk.btree;

public class DefaultTupleBinding<K, V> implements TupleBinding<K, V> {

    private final DataBinding<K> kb;
    
    private final DataBinding<V> vb;

    public static <T> TupleBinding<T, T> create(DataBinding<T> binding) {
        return new DefaultTupleBinding<T, T>(binding, binding);
    }
    
    public DefaultTupleBinding(DataBinding<K> kb, DataBinding<V> vb) {
        this.kb = kb;
        this.vb = vb;
    }

    @Override
    public byte[] objectToKey(K key) {
        return kb.objectToData(key);
    }

    @Override
    public K keyToObject(byte[] key) {
        return kb.dataToObject(key);
    }

    @Override
    public byte[] objectToValue(V value) {
        return vb.objectToData(value);
    }

    @Override
    public V valueToObject(byte[] value) {
        return vb.dataToObject(value);
    }
}
