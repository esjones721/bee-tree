package org.ardverk.btree;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ardverk.btree.NodeProvider.Intent;



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
            = new MemoryNodeProvider(2);
        
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
        
        String[] keys = "G M P X A C D E J K N O R S T U V Y Z".split(" ");
        
        for (int i = 0; i < keys.length; i++) {
            NodeProvider provider 
                = new MemoryNodeProvider(3);
        
            BeeTree<String, String> tree 
                = new BeeTree<String, String>(provider, binding);
    
            for (String key : keys) {
                tree.put(key, key);
            }
            
            System.out.println(provider);
            
            for (String key : keys) {
                try {
                    String removed = tree.remove(key);
                    if (!key.equals(removed)) {
                        throw new IllegalStateException(key + " vs. " + removed);
                    }
                } catch (Exception err) {
                    throw new IllegalStateException("key=" + key, err);
                }
                
                System.out.println("after: " + key + ", " + provider);
            }
        }
    }
    
    private static void test4() {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        FileSystemNodeProvider provider 
            = new FileSystemNodeProvider("tree", 3, Integer.MAX_VALUE, 
                    0L, TimeUnit.MILLISECONDS);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        /*for (int i = 0; i < 1000; i++) {
            String key = "Hello-" + i;
            tree.put(key, key);
        }
        
        for (int i = 0; i < 500; i++) {
            String key = "Hello-" + i;
            String value = tree.remove(key);
            
            if (!key.equals(value)) {
                throw new IllegalStateException(key + " vs. " + value);
            }
        }*/
        
        String[] keys = "G M P X A C D E J K N O R S T U V Y Z".split(" ");
        
        if (tree.isEmpty()) {
            
            System.out.println("INSERT");
            
            for (String key : keys) {
                tree.put(key, key);
            }
            
        } else {
            
            System.out.println("REMOVE");
            
            for (String key : keys) {
                String value = tree.remove(key);
                
                if (!key.equals(value)) {
                    throw new IllegalStateException(key + " vs. " + value);
                }
            }
        }
        
        System.out.println(provider);
        provider.close();
        
        /*RootNode rn = provider.getRoot();
        Node root = rn.getRoot();
        
        System.out.println(root);
        
        for (NodeId nodeId : root.getNodes()) {
            Node node = provider.get(nodeId, Intent.READ);
            
            System.out.println(node);
        }*/
    }
    
    private static void test5() throws InterruptedException {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        FileSystemNodeProvider provider 
            = new FileSystemNodeProvider("tree", 128, Integer.MAX_VALUE, 
                    0L, TimeUnit.MILLISECONDS);
        
        final BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        final CountDownLatch latch = new CountDownLatch(2);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10000; i++) {
                        String key = "Key-" + i;
                        tree.put(key, key);
                        
                        if ((i % 1000) == 0) {
                            System.out.println("A: " + i);
                        }
                    }
                    
                    System.out.println("DONE: A");
                } finally {
                    latch.countDown();
                }
            }
        };
        
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10000; i++) {
                        String key = "Key-" + (10000 + i);
                        tree.put(key, key);
                        
                        if ((i % 1000) == 0) {
                            System.out.println("B: " + i);
                        }
                    }
                    
                    System.out.println("DONE: B");
                } finally {
                    latch.countDown();
                }
            }
        };
        
        (new Thread(task1)).start();
        task2.run();
        
        latch.await();
        provider.close();
    }
    
    private static void test6() {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        FileSystemNodeProvider provider 
            = new FileSystemNodeProvider("tree", 128, Integer.MAX_VALUE, 
                    0L, TimeUnit.MILLISECONDS);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        for (int i = 0; i < 20000; i++) {
            String key = "Key-" + i;
            String value = tree.get(key);
            
            if (!key.equals(value)) {
                throw new IllegalStateException(key + " vs. " + value);
            }
        }
        
        provider.close();
    }
    
    public static void main(String[] args) throws InterruptedException {
        /*for (int i = 0; i < 1000; i++) {
            test();
        }*/
        
        //test();
        //test2();
        //test3();
        //test4();
        //test5();
        test6();
    }
}
