package org.ardverk.btree;


public class Node2<K, V> {

    private final K key;
    
    private final V value;
    
    public final Page2.Id pageId;
    
    public Node2(K key, V value) {
        this(key, value, null);
    }
    
    public Node2(Node2<? extends K, ? extends V> node, Page2.Id pageId) {
        this(node.key, node.value, pageId);
    }
    
    public Node2(Node2<? extends K, ? extends V> node, V value) {
        this(node.key, value, node.pageId);
    }
    
    public Node2(K key, V value, Page2.Id pageId) {
        this.key = key;
        this.value = value;
        this.pageId = pageId;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Page2.Id getPageId() {
        return pageId;
    }
    
    @Override
    public String toString() {
        return key + "=" + value + " -> " + pageId;
    }
}
