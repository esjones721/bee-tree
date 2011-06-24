package org.ardverk.btree3;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryNodeProvider<K, V> implements NodeProvider<K, V> {

    public final Map<NodeId, Node<K, V>> nodes 
        = new LinkedHashMap<NodeId, Node<K, V>>();
    
    private final Comparator<? super K> comparator;
    
    public InMemoryNodeProvider() {
        this(DefaultComparator.create());
    }
    
    public InMemoryNodeProvider(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Node<K, V> get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
    
    @Override
    public Node<K, V> allocate(NodeId init) {
        Node<K, V> node = new Node<K, V>(new NodeId());
        
        if (init != null) {
            node.addLast(init);
        }
        
        nodes.put(node.getNodeId(), node);
        return node;
    }

    @Override
    public void free(Node<? extends K, ? extends V> node) {
        nodes.remove(node.getNodeId());
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }
    
    @Override
    public String toString() {
        return nodes.toString();
    }
}
