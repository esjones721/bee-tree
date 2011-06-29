package org.ardverk.btree;

public class DefaultTupleBinding<K, V> implements TupleBinding<K, V> {
    
    public static <T> TupleBinding<T, T> create(Binding<T> binding) {
        return new DefaultTupleBinding<T, T>(binding, binding);
    }
    
    public static <K, V> TupleBinding<K, V> create(Binding<K> kb, Binding<V> vb) {
        return new DefaultTupleBinding<K, V>(kb, vb);
    }
    
    private final Binding<K> kb;
    
    private final Binding<V> vb;
    
    public DefaultTupleBinding(Binding<K> kb, Binding<V> vb) {
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
