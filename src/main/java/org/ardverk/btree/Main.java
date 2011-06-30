package org.ardverk.btree;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class Main {
    
    public static void main(String[] args) throws InterruptedException {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider = new MemoryNodeProvider(2);
        
        final BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        final CountDownLatch latch = new CountDownLatch(2);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        tree.put("Hello-" + i, "World");
                    }
                } finally {
                    latch.countDown();
                }
            }
        };
        
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        tree.put("Hello-" + (100+i), "World");
                    }
                } finally {
                    latch.countDown();
                }
            }
        };
        
        (new Thread(task1)).start();
        task2.run();
        
        boolean success = latch.await(5L, TimeUnit.SECONDS);
        System.out.println(success);
        System.out.println(tree);
    }
}
