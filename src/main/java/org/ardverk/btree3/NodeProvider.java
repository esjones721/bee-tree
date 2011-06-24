package org.ardverk.btree3;

import java.util.Comparator;

import org.ardverk.btree3.Node.Id;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node<K, V> allocate(Id init);
    
    public void free(Node<? extends K, ? extends V> node);
    
    public Node<K, V> get(Node.Id nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
