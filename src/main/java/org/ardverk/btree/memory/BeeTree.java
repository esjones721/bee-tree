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

package org.ardverk.btree.memory;

import java.util.Iterator;
import java.util.Map;

import org.ardverk.btree.AbstractBeeTree;
import org.ardverk.btree.Node;
import org.ardverk.btree.Node.Median;
import org.ardverk.btree.NodeProvider;
import org.ardverk.btree.NodeProvider.Intent;
import org.ardverk.btree.Tuple;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> {

    final NodeProvider<K, V> provider;
    
    volatile Node<K, V> root;
    
    public BeeTree() {
        this(new DefaultNodeProvider<K, V>());
    }
    
    public BeeTree(NodeProvider<K, V> provider) {
        this.provider = provider;
        
        root = provider.allocate(true);
    }
    
    @Override
    public V put(K key, V value) {
        Tuple<K, V> existing = null;
        synchronized (provider) {
            if (root.isOverflow()) {
                Median<K, V> median = root.split(provider);
                
                Node<K, V> tmp = provider.allocate(false);
                
                tmp.addFirstNodeId(root.getNodeId());
                tmp.addMedian(median);
                
                root = tmp;
            }
            
            existing = root.put(provider, key, value);
        }
        return existing != null ? existing.getValue() : null;
    }
    
    @Override
    public V remove(K key) {
        Tuple<K, V> tuple = null;
        
        synchronized (provider) {
            tuple = root.remove(provider, key);
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node<K, V> tmp = root.firstNode(provider, Intent.READ);
                provider.free(root);
                root = tmp;
            }
        }
        
        return tuple != null ? tuple.getValue() : null;
    }
    
    @Override
    public V get(K key) {
        Tuple<K, V> tuple = root.get(provider, key);
        return tuple != null ? tuple.getValue() : null;
    }
    
    @Override
    public boolean contains(K key) {
        Tuple<K, V> tuple = root.get(provider, key);
        return tuple != null;
    }
    
    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        return root.ceilingTuple(provider, key);
    }
    
    @Override
    public Map.Entry<K, V> firstEntry() {
        return root.firstTuple(provider, Intent.READ);
    }
    
    @Override
    public Map.Entry<K, V> lastEntry() {
        return root.lastTuple(provider, Intent.READ);
    }
    
    @Override
    public void clear() {
        synchronized (provider) {
            Node<K, V> tmp = root;
            root = provider.allocate(true);
            
            provider.free(tmp);
        }
    }
    
    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }
    
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return root.iterator(provider);
    }
    
    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        return iterator(key, true);
    }
    
    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive) {
        return root.iterator(provider, key, inclusive);
    }
}
