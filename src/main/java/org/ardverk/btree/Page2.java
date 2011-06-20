package org.ardverk.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree.PageProvider2.Intent;

class Page2<K, V> implements RandomAccess, Iterable<Node2<K, V>> {

    private static final int t = 4;
    
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
        return size() >= 2 * t - 1;
    }
    
    public void add(Node2<K, V> node) {
        nodes.add(node);
    }
    
    public void add(int index, Node2<K, V> node) {
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
            
            if (index < 0) {
                index = -index - 1;
            }
            
            index = Math.min(index, nodes.size()-1);
            
            Node2<K, V> node = get(index);
            
            if (key.equals(node.getKey())) {
                return node.getValue();
            }
            
            if (!leaf) {
                Page2.Id pageId = node.getPageId();
                Page2<K, V> child = provider.get(pageId, Intent.READ);
                return child.get(provider, key);
            }
        }
        
        return null;
    }
    
    public void put(PageProvider2<K, V> provider, K key, V value) {
        Comparator<? super K> comparator = provider.comparator();
        
        int index = NodeUtils.binarySearch(nodes, key, comparator);
        
        if (leaf) {
            if (index < 0) {
                Node2<K, V> node = new Node2<K, V>(key, value);
                nodes.add(-index - 1, node);
            } else {
                Node2<K, V> existing = nodes.get(index);
                Node2<K, V> node = new Node2<K, V>(existing, value);
                nodes.set(index, node);
            }
            
        } else {
            
            if (index < 0) {
                index = -index - 1;
            }
            
            index = Math.min(index, nodes.size()-1);
            
            Node2<K, V> foo = get(index);
            Page2<K, V> page = provider.get(foo.getPageId(), Intent.WRITE);
            
            if (page.isFull()) {
                
                System.out.println("Splitting for: key=" + key + ", foo=" 
                        + foo + ", this=" + this + ", page=" + page);
                
                Node2<K, V> median = page.split(provider);
                add(index, median);
                /*add(median);
                Collections.sort(nodes, new Comparator<Node2<K, V>>() {
                    @Override
                    public int compare(Node2<K, V> o1, Node2<K, V> o2) {
                        return ((Comparable<K>)o1.getKey()).compareTo(o2.getKey());
                    }
                });*/
                page = provider.get(median.getPageId(), Intent.WRITE);
                
                System.out.println("AFTER: " + this);
                System.out.println(key + " into " + median + " vs. " + foo);
            }
            
            page.put(provider, key, value);
        }
    }
    
    public Node2<K, V> split(PageProvider2<K, V> provider) {
        Page2<K, V> dst = provider.create(leaf);
        
        int size = size();
        int m = size/2;
        
        List<Node2<K, V>> copy = new ArrayList<Node2<K,V>>(nodes);
        
        Node2<K, V> median = remove(m);
        for (int i = 0; i < m; i++) {
            // TODO: Optimize remove() !!!
            dst.add(remove(0));
        }
        
        System.out.println("SPLIT: " + pageId + " from " + copy 
                + " to " + dst.nodes + "(" + dst.pageId + ") and " 
                + nodes + "(" + pageId + ")");
        
        System.out.println("LEFT: " + dst);
        System.out.println("MEDIAN: " + new Node2<K, V>(median, dst.getPageId()));
        System.out.println("RIGHT: " + this);
        
        return new Node2<K, V>(median, dst.getPageId());
    }
    
    @Override
    public String toString() {
        return pageId + " -> " + nodes;
    }
    
    public String toString(PageProvider2<K, V> provider) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(pageId).append("\n");
        for (Node2<K, V> node : nodes) {
            sb.append(" ").append(node.getKey())
                .append("=").append(node.getValue())
                .append(" -> ").append(node.getPageId())
                .append("\n");
            
            Page2.Id foo = node.getPageId();
            if (foo != null) {
                Page2<K, V> page = provider.get(foo, Intent.READ);
                sb.append(page.toString(provider));
            }
        }
        
        return sb.toString();
    }
    
    public static class Id {
        
        private static final AtomicInteger COUNTER = new AtomicInteger();
        
        private final int value = COUNTER.incrementAndGet();
        
        @Override
        public int hashCode() {
            return value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Id)) {
                return false;
            }
            
            Id other = (Id)o;
            return value == other.value;
        }
        
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}
