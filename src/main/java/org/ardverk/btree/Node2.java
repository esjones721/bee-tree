package org.ardverk.btree;

import org.ardverk.btree.PageProvider2.Intent;

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
    
    public Node2<K, V> split(PageProvider2<K, V> provider) {
        Page2<K, V> src = getPage(provider, Intent.WRITE);
        Page2<K, V> dst = provider.create(false);
        
        int size = src.size();
        int m = size/2;
        
        Node2<K, V> median = src.remove(m);
        for (int i = 1; i < m; i++) {
            // TODO: Optimize remove() !!!
            dst.add(src.remove(m));
        }
        
        return new Node2<K, V>(median, dst.getPageId());
    }
    
    public Page2<K, V> getPage(PageProvider2<K, V> provider, Intent intent) {
        return provider.get(pageId, intent);
    }
    
    @Override
    public String toString() {
        return key != null ? key.toString() : "null";
    }
}
