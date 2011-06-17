package org.ardverk.btree;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ardverk.btree.Page.Id;

public class DefaultPageProvider<K, V> implements PageProvider<K, V> {

    private final Map<Id, Page<K, V>> pages 
        = new ConcurrentHashMap<Id, Page<K, V>>();
    
    private final Comparator<? super K> comparator;
    
    private volatile Page<K, V> root;
    
    public DefaultPageProvider() {
        this(DefaultComparator.create());
    }
    
    public DefaultPageProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
        
        root = new Page<K, V>(comparator, true);
        pages.put(root.getPageId(), root);
    }
    
    @Override
    public Id getRootId() {
        return root.getPageId();
    }

    @Override
    public Page<K, V> get(Id nodeId, Intent intent) {
        Page<K, V> node = pages.get(nodeId);
        
        /*if (intent == Intent.WRITE) {
            Page<K, V> shadow = node.shadow();
            
            if (shadow != node) {
                add(shadow);
            }
            
            node = shadow;
        }*/
        
        return node;
    }
    
    
    @Override
    public void add(Page<K, V> page) {
        pages.put(page.getPageId(), page);
    }

    @Override
    public Page<K, V> get(Node<? extends K, ? extends V> node, Intent intent) {
        return get(node.getPageId(), intent);
    }
    
    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }
    
    private static class DefaultComparator<K> implements Comparator<K> {
        
        private static final DefaultComparator<?> COMPARATOR 
            = new DefaultComparator<Object>();
        
        @SuppressWarnings("unchecked")
        public static <K> Comparator<K> create() {
            return (Comparator<K>)COMPARATOR;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(K o1, K o2) {
            return ((Comparable<K>)o1).compareTo(o2);
        }
    }
    
    /*private static class IdentityHashSet<E> extends AbstractSet<E> {
        
        private static final Object PRESENT = new Object();
        
        private final Map<E, Object> map;
        
        public IdentityHashSet() {
            this(16);
        }
        
        public IdentityHashSet(int initialSize) {
            map = new IdentityHashMap<E, Object>(initialSize);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return map.keySet().removeAll(c);
        }

        @Override
        public boolean add(E element) {
            return map.put(element, PRESENT) == null;
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public boolean contains(Object e) {
            return map.keySet().contains(e);
        }

        @Override
        public Iterator<E> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public boolean remove(Object e) {
            return map.remove(e) == PRESENT;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Object[] toArray() {
            return map.keySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return map.keySet().toArray(a);
        }

        @Override
        public String toString() {
            return map.keySet().toString();
        }
    }*/
}
