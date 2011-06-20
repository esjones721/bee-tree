package org.ardverk.btree2;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree2.Node.Id;
import org.ardverk.btree2.Node.Split;

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
        root = create(true);
    }

    @Override
    public Node<K, V> get(Id pageId, Intent intent) {
        Node<K, V> node = nodes.get(pageId);
        return node;
    }
    
    @Override
    public Node<K, V> create(boolean leaf) {
        return add(new Node<K, V>(leaf));
    }

    public Node<K, V> add(Node<K, V> node) {
        nodes.put(node.getId(), node);
        return node;
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }
    
    public void put(K key, V value) {
        if (root.isFull()) {
            Node<K, V> tmp = create(false);
            
            Split<K, V> split = root.split(this);
            
            tmp.add(root.getId());
            tmp.add(split.getMedian());
            tmp.add(split.getNodeId());
            
            root = tmp;
            
            System.out.println(nodes);
        }
        
        root.put(this, key, value);
    }
    
    public static void main(String[] args) {
        DefaultNodeProvider<String, String> t 
            = new DefaultNodeProvider<String, String>();
        
        t.put("A", "A");
        t.put("C", "C");
        t.put("G", "G");
        t.put("N", "N");
        System.out.println(t.root);
        
        t.put("H", "H");
        System.out.println(t.root);
        
        t.put("E", "E");
        t.put("K", "K");
        t.put("Q", "Q");
        System.out.println(t.root);
        
        System.out.println(t.nodes);
        
        t.put("M", "M");
        System.out.println(t.root);
        
        System.out.println(t.nodes);
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
            return ((Comparable<K>)o1).compareTo(o2);
        }
    }
}
