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

import java.util.Iterator;
import java.util.Map;

import org.ardverk.btree.Node.Median;
import org.ardverk.btree.NodeProvider.Intent;
import org.ardverk.btree.memory.DefaultNodeProvider;

public class BeeTree<K, V> implements Iterable<Map.Entry<K, V>> {

    final NodeProvider<K, V> provider;
    
    volatile Node<K, V> root;
    
    public BeeTree() {
        this(new DefaultNodeProvider<K, V>());
    }
    
    public BeeTree(NodeProvider<K, V> provider) {
        this.provider = provider;
        
        root = provider.allocate(true);
    }
    
    public V put(K key, V value) {
        Entry<K, V> existing = null;
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
    
    public V remove(K key) {
        Entry<K, V> entry = null;
        
        synchronized (provider) {
            entry = root.remove(provider, key);
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node<K, V> tmp = root.firstNode(provider, Intent.READ);
                provider.free(root);
                root = tmp;
            }
        }
        
        return entry != null ? entry.getValue() : null;
    }
    
    public V get(K key) {
        Entry<K, V> entry = root.get(provider, key);
        return entry != null ? entry.getValue() : null;
    }
    
    public Map.Entry<K, V> ceilingEntry(K key) {
        return root.ceilingEntry(provider, key);
    }
    
    public Map.Entry<K, V> firstEntry() {
        return root.firstEntry(provider, Intent.READ);
    }
    
    public Map.Entry<K, V> lastEntry() {
        return root.lastEntry(provider, Intent.READ);
    }
    
    public void clear() {
        synchronized (provider) {
            Node<K, V> tmp = root;
            root = provider.allocate(true);
            
            provider.free(tmp);
        }
    }
    
    public boolean isEmpty() {
        return root.isEmpty();
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
        return root.iterator(provider);
    }
    
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        return iterator(key, true);
    }
    
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive) {
        return root.iterator(provider, key, inclusive);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        if (!isEmpty()) {
            for (Map.Entry<K, V> entry : this) {
                sb.append(entry).append(", ");
            }
            
            sb.setLength(sb.length()-2);
        }
        
        sb.append("]");
        return sb.toString();
    }
}
