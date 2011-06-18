package org.ardverk.btree;

import java.util.Comparator;

public interface PageProvider2<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Page2.Id getRootId();
    
    public Page2<K, V> get(Page2.Id nodeId, Intent intent);
    
    public Page2<K, V> create(boolean leaf);
    
    public Page2<K, V> add(Page2<K, V> node);
    
    public Comparator<? super K> comparator();
}
