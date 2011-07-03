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

import junit.framework.TestCase;

import org.junit.Test;

public class BeeTreeTest {

    private static final TupleBinding<String, String> binding 
        = DefaultTupleBinding.create(StringBinding.BINDING);
    
    @Test
    public void put() {
        NodeProvider provider = new MemoryNodeProvider(128);
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        int count = 10000;
        for (int i = 0; i < count; i++) {
            String key = "Key-" + i;
            
            String existing = tree.put(key, key);
            TestCase.assertNull(existing);
        }
        
        TestCase.assertEquals(count, tree.size());
    }
    
    @Test
    public void remove() {
        NodeProvider provider = new MemoryNodeProvider(128);
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        int count = 10000;
        for (int i = 0; i < count; i++) {
            String key = "Key-" + i;
            tree.put(key, key);
        }
        
        for (int i = 0; i < count; i++) {
            String key = "Key-" + i;
            String value = tree.remove(key);
            TestCase.assertEquals(key, value);
        }
        
        TestCase.assertEquals(0, tree.size());
    }
    
    @Test
    public void get() {
        NodeProvider provider = new MemoryNodeProvider(128);
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        int count = 10000;
        for (int i = 0; i < count; i++) {
            String key = "Key-" + i;
            tree.put(key, key);
        }
        
        for (int i = 0; i < count; i++) {
            String key = "Key-" + i;
            String value = tree.get(key);
            TestCase.assertEquals(key, value);
        }
    }
}