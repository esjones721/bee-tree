package org.ardverk.btree;

public interface Binding<T> {

    public byte[] objectToData(T obj);
    
    public T dataToObject(byte[] data);
}
