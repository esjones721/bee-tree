package org.ardverk.btree;

public interface DataBinding<T> {

    public byte[] objectToData(T obj);
    
    public T dataToObject(byte[] data);
}
