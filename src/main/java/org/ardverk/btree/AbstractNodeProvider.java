/*
 * Copyright 2011 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.btree;

import java.util.Comparator;

public abstract class AbstractNodeProvider<K, V> 
        implements NodeProvider<K, V>{ 

    protected final Comparator<? super K> comparator;
    
    protected volatile Node<K, V> root = null;
    
    public AbstractNodeProvider() {
        this(KeyComparator.create());
    }
    
    public AbstractNodeProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }
    
    @Override
    public Node<K, V> getRoot() {
        return root;
    }

    @Override
    public void setRoot(Node<K, V> root) {
        this.root = root;
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
