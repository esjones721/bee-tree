package org.ardverk.btree2;

import java.util.Comparator;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node<K, V> create(boolean leaf);
    
    public Node<K, V> register(Node<K, V> node);
    
    public Node<K, V> get(Node.Id nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
