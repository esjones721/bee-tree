package org.ardverk.btree.fs;

public interface EntryBinding<E> {

    public E entryToObject(byte[] entry);
    
    public byte[] objectToEntry(E element);
}
