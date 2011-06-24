package org.ardverk.btree3;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree3.Node.Id;
import org.ardverk.btree3.Node.Median;


public class DefaultNodeProvider<K, V> implements NodeProvider<K, V> {

    public final Map<Id, Node<K, V>> nodes 
        = new LinkedHashMap<Id, Node<K, V>>();
    
    private final Comparator<? super K> comparator;
    
    public volatile Node<K, V> root;
    
    public DefaultNodeProvider() {
        this(DefaultComparator.create());
    }
    
    public DefaultNodeProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
        root = allocate(null);
    }

    @Override
    public Node<K, V> get(Id pageId, Intent intent) {
        Node<K, V> node = nodes.get(pageId);
        return node;
    }
    
    @Override
    public Node<K, V> allocate(Id init) {
        Node<K, V> node = new Node<K, V>(init);
        nodes.put(node.getId(), node);
        return node;
    }

    @Override
    public void free(Node<? extends K, ? extends V> node) {
        nodes.remove(node.getId());
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }
    
    public void put(K key, V value) {
        if (root.isFull()) {
            Median<K, V> median = root.split(this);
            
            Node<K, V> tmp = allocate(root.getId());
            
            tmp.add(median.getEntry());
            tmp.add(median.getNodeId());
            
            root = tmp;
        }
        
        root.put(this, key, value);
    }
    
    public V get(K key) {
        Entry<K, V> entry = root.get(this, key);
        return entry != null ? entry.getValue() : null;
    }
    
    public void remove(K key) {
        root.remove(this, key);
    }
    
    public static void main(String[] args) {
        DefaultNodeProvider<String, String> t 
            = new DefaultNodeProvider<String, String>();
        
        t.put("3", "3");
        t.put("5", "5");
        t.put("9", "9");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("7", "7");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("1", "1");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("2", "2");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("8", "8");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("6", "6");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("0", "0");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        t.put("4", "4");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        System.out.println();
        t.remove("8");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
        System.out.println();
        t.remove("7");
        System.out.println("ROOT: " + t.root);
        System.out.println("NODES: " + t.nodes);
        
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
        
        /*long startTime = System.currentTimeMillis();
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
        System.out.println("Done: " + count + ", " + time);*/
    }
    
    private static class DefaultComparator<K> implements Comparator<K> {
        
        private static final DefaultComparator<?> COMPARATOR 
            = new DefaultComparator<Object>();
        
        @SuppressWarnings("unchecked")
        public static <K> Comparator<K> create() {
            return (Comparator<K>)COMPARATOR;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(K o1, K o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            }
            
            return ((Comparable<K>)o1).compareTo(o2);
        }
    }
}