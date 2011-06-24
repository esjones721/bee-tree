package org.ardverk.btree;

import java.util.Comparator;

class DefaultComparator<K> implements Comparator<K> {
    
    private static final Comparator<?> COMPARATOR 
        = new DefaultComparator<Object>();
    
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