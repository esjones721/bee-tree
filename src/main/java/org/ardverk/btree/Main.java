package org.ardverk.btree;



public class Main {
    
    public static void main(String[] args) throws InterruptedException {
        TupleBinding<String, String> binding 
            = DefaultTupleBinding.create(StringBinding.BINDING);
        
        /*FileSystemNodeProvider provider 
            = new FileSystemNodeProvider("tree", 128, 64);*/
        
        NodeProvider provider 
            = new MemoryNodeProvider(2);
        
        BeeTree<String, String> tree 
            = new BeeTree<String, String>(provider, binding);
        
        System.out.println(tree.size());
        
        for (int i = 0; i < 10; i++) {
            tree.put("Hello-" + i, "World-" + i);
        }
        
        System.out.println("PROVIDER: " + provider);
        System.out.println("TREE: " + tree);
        
        for (int i = 0; i < 5; i++) {
            String key = "Hello-" + i;
            String expected = "World-" + i;
            String value = tree.remove(key);
            
            if (!expected.equals(value)) {
                throw new IllegalStateException(expected + " vs. " + value);
            }
        }
        
        for (int i = 0; i < 10; i++) {
            System.out.println(tree.get("Hello-" + i));
        }
        
        //provider.close();
    }
}
