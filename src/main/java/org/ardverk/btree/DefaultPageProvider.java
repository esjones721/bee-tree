package org.ardverk.btree;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.Page2.Id;

public class DefaultPageProvider<K, V> implements PageProvider2<K, V> {

    public final Map<Id, Page2<K, V>> pages 
        = new LinkedHashMap<Id, Page2<K, V>>();
    
    private final Comparator<? super K> comparator;
    
    public volatile Page2<K, V> root;
    
    public DefaultPageProvider() {
        this(DefaultComparator.create());
    }
    
    public DefaultPageProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
        
        root = create(true);
    }
    
    @Override
    public Id getRootId() {
        return root.getPageId();
    }

    @Override
    public Page2<K, V> get(Id pageId, Intent intent) {
        Page2<K, V> node = pages.get(pageId);
        
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
    public Page2<K, V> create(boolean leaf) {
        return add(new Page2<K, V>(leaf));
    }

    @Override
    public Page2<K, V> add(Page2<K, V> node) {
        pages.put(node.getPageId(), node);
        return node;
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
