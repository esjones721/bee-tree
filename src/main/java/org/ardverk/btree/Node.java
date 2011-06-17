package org.ardverk.btree;


class Node<K, V> {

    private final K key;
    
    private final V value;
    
    private final Page.Id pageId;
    
    public Node(K key, V value) {
        this(key, value, null);
    }
    
    public Node(K key, Page.Id pageId) {
        this(key, null, pageId);
    }
    
    private Node(K key, V value, Page.Id pageId) {
        if (pageId != null && value != null) {
            throw new IllegalStateException();
        }
        
        this.key = key;
        this.value = value;
        this.pageId = pageId;
    }

    public boolean isLeaf() {
        return pageId == null;
    }
    
    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    
    public Page.Id getPageId() {
        return pageId;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        
        if (isLeaf()) {
            sb.append("=").append(value);
        } else {
            sb.append(" -> ").append(pageId);
        }
        
        return sb.toString();
    }
}
