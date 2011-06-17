package org.ardverk.btree;

import java.util.Comparator;

public interface PageProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Page.Id getRootId();
    
    public Page<K, V> get(Page.Id nodeId, Intent intent);
    
    public Page<K, V> get(Node<? extends K, ? extends V> node, Intent intent);
    
    public void add(Page<K, V> node);
    
    public Comparator<? super K> comparator();
}
