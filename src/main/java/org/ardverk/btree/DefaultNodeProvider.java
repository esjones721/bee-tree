package org.ardverk.btree;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ardverk.btree.Node.Id;

public class DefaultNodeProvider<K, V> implements NodeProvider<K, V> {

    private final Map<Id, Node<K, V>> nodes 
        = new ConcurrentHashMap<Id, Node<K, V>>();
    
    private final Comparator<? super K> comparator;
    
    private volatile Node<K, V> root;
    
    public DefaultNodeProvider() {
        this(DefaultComparator.create());
    }
    
    public DefaultNodeProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
        
        root = new Node<K, V>(this, true);
        nodes.put(root.getNodeId(), root);
    }
    
    @Override
    public Id getRootId() {
        return root.getNodeId();
    }

    @Override
    public Node<K, V> get(Id nodeId, Intent intent) {
        Node<K, V> node = nodes.get(nodeId);
        
        if (intent == Intent.WRITE) {
            Node<K, V> shadow = node.shadow();
            
            if (shadow != node) {
                nodes.put(shadow.getNodeId(), shadow);
            }
            
            /*if (node == root) {
                root = shadow;
            }*/
            
            node = shadow;
        }
        
        return node;
    }
    
    @Override
    public Comparator<? super K> comparator() {
        return comparator;
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
