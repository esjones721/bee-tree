package org.ardverk.btree;

import java.util.ArrayList;
import java.util.List;

public class Node<K, V> {

    private final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
    
    private final List<Id> nodes = new ArrayList<Id>();
    
    public static class Id {
        
    }
}
