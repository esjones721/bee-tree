package org.ardverk.btree;

import java.util.Comparator;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node<K, V> allocate(boolean leaf);
    
    public void free(Node<? extends K, ? extends V> node);
    
    public Node<K, V> get(NodeId nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
