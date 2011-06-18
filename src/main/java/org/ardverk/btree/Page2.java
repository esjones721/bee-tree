package org.ardverk.btree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import org.ardverk.btree.PageProvider2.Intent;

class Page2<K, V> implements RandomAccess, Iterable<Node2<K, V>> {

    private final Id pageId;
    
    private final List<Node2<K, V>> nodes;
    
    private final boolean leaf;
    
    public Page2(boolean leaf) {
        this(new Id(), leaf);
    }
    
    public Page2(Id pageId, boolean leaf) {
        this(pageId, new ArrayList<Node2<K, V>>(), leaf);
    }
    
    public Page2(Id pageId, List<Node2<K, V>> nodes, boolean leaf) {
        this.pageId = pageId;
        this.nodes = nodes;
        this.leaf = leaf;
    }
    
    public Id getPageId() {
        return pageId;
    }
    
    @Override
    public Iterator<Node2<K, V>> iterator() {
        return nodes.iterator();
    }
    
    public boolean isLeaf() {
        return leaf;
    }
    
    public int size() {
        return nodes.size();
    }
    
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    public boolean isFull() {
        return size() >= 4;
    }
    
    public void add(Node2<K, V> node) {
        if (node == null) {
            Thread.dumpStack();
        }
        
        nodes.add(node);
    }
    
    public void add(int index, Node2<K, V> node) {
        if (node == null) {
            Thread.dumpStack();
        }
        nodes.add(index, node);
    }
    
    public Node2<K, V> get(int index) {
        return nodes.get(index);
    }
    
    public Node2<K, V> remove(int index) {
        return nodes.remove(index);
    }
    
    public V get(PageProvider2<K, V> provider, K key) {
        if (!isEmpty()) {
            
            Comparator<? super K> comparator = provider.comparator();
            int index = NodeUtils.binarySearch(nodes, key, comparator);
            
            Node2<K, V> node = get(Math.abs(index));
            
            if (key.equals(node.getKey())) {
                return node.getValue();
            }
            
            Page2<K, V> child = provider.get(node.getPageId(), Intent.READ);
            return child.get(provider, key);
        }
        
        return null;
    }
    
    public void put(PageProvider2<K, V> provider, K key, V value) {
        Comparator<? super K> comparator = provider.comparator();
        
        int index = NodeUtils.binarySearch(nodes, key, comparator);
        System.out.println("a: " + System.identityHashCode(this) + ": " + nodes.size() + ", " + index + ", " + key);
        
        if (leaf) {
            if (index < 0) {
                Node2<K, V> node = new Node2<K, V>(key, value);
                nodes.add(-index - 1, node);
            } else {
                Node2<K, V> existing = nodes.get(index);
                Node2<K, V> node = new Node2<K, V>(existing, value);
                nodes.set(index, node);
            }
            
            System.out.println(nodes);
            
        } else {
            
            System.out.println("b: " + System.identityHashCode(this) + ": " + nodes.size() + ", " + index + ", " + key);
            
            if (index < 0) {
                index = -index;
            }
            
            index = Math.min(index, nodes.size()-1);
            
            Node2<K, V> foo = get(index);
            Page2<K, V> page = foo.getPage(provider, Intent.WRITE);
            
            if (page.isFull()) {
                Node2<K, V> bla = page.split2(provider);
                add(index, bla);
                page = provider.get(bla.getPageId(), Intent.WRITE);
            }
            
            page.put(provider, key, value);
        }
    }
    
    /*public Page2<K, V> split(PageProvider2<K, V> provider, int index) {
        return split(provider, index, nodes.get(index));
    }
    
    public Page2<K, V> split(PageProvider2<K, V> provider, int index, Node2<K, V> node) {
        Node2<K, V> bla = node.split(provider);
        nodes.add(index, bla);
        return bla.getPage(provider, Intent.WRITE);
    }*/
    
    public Node2<K, V> split2(PageProvider2<K, V> provider) {
        Page2<K, V> dst = provider.create(leaf);
        
        int size = size();
        int m = size/2;
        
        Node2<K, V> median = remove(m);
        for (int i = 1; i < m; i++) {
            // TODO: Optimize remove() !!!
            dst.add(remove(m));
        }
        
        return new Node2<K, V>(median, dst.getPageId());
    }
    
    public static class Id {
        
    }
}
