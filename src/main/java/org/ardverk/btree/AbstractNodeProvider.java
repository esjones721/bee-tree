package org.ardverk.btree;

import java.util.Comparator;

public abstract class AbstractNodeProvider<K, V> 
        implements NodeProvider<K, V>{ 

    private final Comparator<? super K> comparator;
    
    public AbstractNodeProvider() {
        this(KeyComparator.create());
    }
    
    public AbstractNodeProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }
    
    private static class KeyComparator<K> implements Comparator<K> {
        
        private static final Comparator<?> COMPARATOR 
            = new KeyComparator<Object>();
        
        @SuppressWarnings("unchecked")
        public static <K> Comparator<K> create() {
            return (Comparator<K>)COMPARATOR;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(K o1, K o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            }
            
            return ((Comparable<K>)o1).compareTo(o2);
        }
    }
}
