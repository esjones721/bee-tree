package org.ardverk.btree;


public class Main {
    
    public static void main(String[] args) {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        NodeProvider provider = new MemoryNodeProvider(2);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        for (int i = 0; i < 100; i++) {
            tree.put("Hello-" + i, "World");
        }
        
        System.out.println(tree);
    }
}
