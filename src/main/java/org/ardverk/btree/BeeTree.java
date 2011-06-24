package org.ardverk.btree;

import java.util.Arrays;
import java.util.Map;

import org.ardverk.btree.Node.Median;
import org.ardverk.btree.NodeProvider.Intent;
import org.ardverk.btree.memory.InMemoryNodeProvider;

public class BeeTree<K, V> {

    private final NodeProvider<K, V> provider;
    
    private volatile Node<K, V> root;
    
    public BeeTree() {
        this(new InMemoryNodeProvider<K, V>());
    }
    
    public BeeTree(NodeProvider<K, V> provider) {
        this.provider = provider;
        
        root = provider.allocate(true);
    }
    
    public void put(K key, V value) {
        synchronized (provider) {
            if (root.isOverflow()) {
                Median<K, V> median = root.split(provider);
                
                Node<K, V> tmp = provider.allocate(false);
                
                tmp.addFirst(root.getNodeId());
                tmp.add(median);
                
                root = tmp;
            }
            
            root.put(provider, key, value);
        }
    }
    
    public V remove(K key) {
        synchronized (provider) {
            Entry<K, V> entry = root.remove(provider, key);
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node<K, V> tmp = root.firstNode(provider, Intent.READ);
                provider.free(root);
                root = tmp;
            }
            
            return entry != null ? entry.getValue() : null;
        }
    }
    
    public V get(K key) {
        Entry<K, V> entry = root.get(provider, key);
        return entry != null ? entry.getValue() : null;
    }
    
    public Map.Entry<K, V> ceilingEntry(K key) {
        return root.ceilingEntry(provider, key);
    }
    
    public void clear() {
        synchronized (provider) {
            Node<K, V> tmp = root;
            root = provider.allocate(true);
            
            provider.free(tmp);
        }
    }
    
    public static void main(String[] args) {
        
        BeeTree<String, String> t = new BeeTree<String, String>();
        
        t.put("3", "3");
        t.put("5", "5");
        t.put("9", "9");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("7", "7");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println("C: " + t.ceilingEntry("6"));
        System.exit(0);
        
        t.put("1", "1");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("2", "2");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("8", "8");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("6", "6");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("0", "0");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        t.put("4", "4");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("8");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("6");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("4");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("5");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("3");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("2");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("9");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        //System.out.println();
        //t.remove("7");
        //System.out.println("ROOT: " + t.root);
        //System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("1");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("0");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        System.out.println();
        t.remove("BLA");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.provider);
        
        /*t.put("4", "4");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("5", "5");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        System.out.println("---");
        t.put("6", "6");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);*/
        
        //t.put("7", "7");
        
        
        /*t.put("A", "A");
        t.put("D", "D");
        t.put("F", "F");
        t.put("H", "H");
        t.put("L", "L");
        t.put("N", "N");
        t.put("P", "P");
        t.put("X", "X");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);*/
        
        /*t.put("A", "A");
        t.put("C", "C");
        t.put("G", "G");
        t.put("N", "N");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("H", "H");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("E", "E");
        t.put("K", "K");
        t.put("Q", "Q");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("M", "M");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);*/
        
        /*t.put("Z", "Z");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("X", "X");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("XX", "XX");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("XXX", "XXX");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("Roger", "Roger");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        System.out.println("1: " + t.get("N"));
        System.out.println("2: " + t.get("M"));
        System.out.println("3: " + t.get("Q"));
        System.out.println("4: " + t.get("Z"));
        System.out.println("5: " + t.get("X"));
        System.out.println("6: " + t.get("XX"));
        System.out.println("7: " + t.get("XXX"));
        System.out.println("8: " + t.get("Roger"));*/
        
        long startTime = System.currentTimeMillis();
        int count = 0;
        for (int i = 'A'; i <= 'Z'; i++) {
            for (int j = 0; j < 1000; j++) {
                String key = Character.toString((char)i);
                for (int k = 0; k < j; k++) {
                    key += Character.toString((char)i);
                }
                
                t.put(key, key);
                ++count;
                
                String found = t.get(key);
                if (found == null || !found.equals(key)) {
                    throw new IllegalStateException("key=" + key + ", found=" + found);
                }
            }
        }
        
        long time = System.currentTimeMillis() - startTime;
        System.out.println("Done: " + count + ", " + time);
    }
}
