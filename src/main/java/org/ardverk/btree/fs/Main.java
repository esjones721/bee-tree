package org.ardverk.btree.fs;

import org.ardverk.btree.BeeTree;
import org.ardverk.btree.DefaultTupleBinding;
import org.ardverk.btree.NodeProvider;
import org.ardverk.btree.StringBinding;
import org.ardverk.btree.TupleBinding;

public class Main {
    
    public static void main(String[] args) {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider = new FileSystemNodeProvider(null, 2);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        for (int i = 0; i < 100; i++) {
            tree.put("Hello-" + i, "World");
        }
        
        System.out.println(tree);
    }
}
