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
        
        NodeProvider provider = null;
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        tree.put("Hello", "World");
        
        System.out.println(tree);
    }
}
