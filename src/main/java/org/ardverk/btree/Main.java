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
        
        tree.put("5", "5");
        tree.put("9", "9");
        tree.put("3", "3");
        System.out.println(provider);
        
        tree.put("7", "7");
        System.out.println(provider);
        
        tree.put("1", "1");
        System.out.println(provider);
        
        tree.put("2", "2");
        System.out.println(provider);
        
        tree.put("8", "8");
        System.out.println(provider);
        
        tree.put("6", "6");
        System.out.println(provider);
        
        tree.put("0", "0");
        System.out.println(provider);
        
        tree.put("4", "4");
        System.out.println(provider);
        
        /*System.out.println(tree.size());
        
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
        }*/
        
        //provider.close();
    }
}
