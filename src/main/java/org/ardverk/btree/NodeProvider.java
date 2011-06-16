package org.ardverk.btree;

import java.util.Comparator;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node.Id getRootId();
    
    public Node<K, V> get(Node.Id nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
