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
