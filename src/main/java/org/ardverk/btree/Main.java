package org.ardverk.btree;

import java.util.Arrays;
import java.util.Collections;



public class Main {
    
    private static void test() {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider 
            = new MemoryNodeProvider(2);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
    
        String[] keys = "5 9 3 7 1 2 8 6 0 4".split(" ");
        
        for (String key : keys) {
            tree.put(key, key);
        }
        
        Collections.shuffle(Arrays.asList(keys));
        
        System.out.println(provider);
        System.out.println(Arrays.toString(keys));
        
        for (String key : keys) {
            String value = null;
            
            try {
                value = tree.remove(key);
            } catch (Exception err) {
                throw new IllegalStateException("key=" + key, err);
            }
            
            if (!key.equals(value)) {
                throw new IllegalStateException(key + " vs. " + value);
            }
        }
    }
    
    private static void test2() {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider 
            = new MemoryNodeProvider(2.5f);
        
        String[] keys = "C N G A H E K Q M F W L T Z D P R X Y S".split(" ");
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        for (String key : keys) {
            tree.put(key, key);
            
            if (key.equals("Q") || key.equals("M") || key.equals("T")) {
                System.out.println(key);
                System.out.println(provider);
            }
        }
        
        System.out.println(provider);
    }
    
    private static void test3() {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider 
            = new MemoryNodeProvider(2.5f);
        
        String[] keys = "G M P X A C D E J K N O R S T U V Y Z".split(" ");
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
    
        for (String key : keys) {
            tree.put(key, key);
        }
        
        System.out.println(provider);
    }
    
    public static void main(String[] args) throws InterruptedException {
        /*for (int i = 0; i < 1000; i++) {
            test();
        }*/
        
        test();
        //test2();
        //test3();
    }
}
