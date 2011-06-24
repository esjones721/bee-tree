package org.ardverk.btree.memory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.AbstractNodeProvider;
import org.ardverk.btree.Node;
import org.ardverk.btree.NodeId;

public class InMemoryNodeProvider<K, V> extends AbstractNodeProvider<K, V> {

    private static final int t = 2;
    
    public final Map<NodeId, Node<K, V>> nodes 
        = new LinkedHashMap<NodeId, Node<K, V>>();
    
    public InMemoryNodeProvider() {
        super();
    }

    public InMemoryNodeProvider(Comparator<? super K> comparator) {
        super(comparator);
    }

    @Override
    public Node<K, V> get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
    
    @Override
    public Node<K, V> allocate(boolean leaf) {
        NodeId nodeId = new InMemoryNodeId();
        Node<K, V> node = new Node<K, V>(leaf, nodeId, t);
        
        nodes.put(node.getNodeId(), node);
        return node;
    }

    @Override
    public void free(Node<? extends K, ? extends V> node) {
        nodes.remove(node.getNodeId());
    }
    
    @Override
    public String toString() {
        return nodes.toString();
    }
}
