/*
 * Copyright 2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.btree;

import java.util.Iterator;
import java.util.Map;

/**
 * Interface description of a Binary Search Tree (BST).
 */
public interface BST<K, V> extends Iterable<Map.Entry<K, V>> {

    /**
     * 
     */
    public V put(K key, V value);

    /**
     * 
     */
    public V remove(K key);

    /**
     * 
     */
    public V get(K key);
    
    /**
     * 
     */
    public boolean contains(K key);

    /**
     * 
     */
    public Map.Entry<K, V> ceilingEntry(K key);

    /**
     * 
     */
    public Map.Entry<K, V> firstEntry();

    /**
     * 
     */
    public Map.Entry<K, V> lastEntry();

    /**
     * 
     */
    public void clear();

    /**
     * 
     */
    public int size();
    
    /**
     * 
     */
    public boolean isEmpty();
    
    /**
     * 
     */
    public Iterator<Map.Entry<K, V>> iterator(K key);

    /**
     * 
     */
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive);

}