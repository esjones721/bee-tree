package org.ardverk.btree3;

import java.util.Comparator;

import org.ardverk.btree3.Node.Id;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node<K, V> create(Id childId);
    
    public Node<K, V> register(Node<K, V> node);
    
    public void unregister(Node<K, V> node);
    
    public Node<K, V> get(Node.Id nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
