package org.ardverk.btree.memory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.AbstractNodeProvider;
import org.ardverk.btree.Node;
import org.ardverk.btree.NodeId;

public class DefaultNodeProvider<K, V> extends AbstractNodeProvider<K, V> {

    public final Map<NodeId, Node<K, V>> nodes 
        = new LinkedHashMap<NodeId, Node<K, V>>();
    
    public DefaultNodeProvider() {
        super();
    }

    public DefaultNodeProvider(Comparator<? super K> comparator, int t) {
        super(comparator, t);
    }

    public DefaultNodeProvider(Comparator<? super K> c) {
        super(c);
    }

    public DefaultNodeProvider(int t) {
        super(t);
    }

    @Override
    public Node<K, V> get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
    
    @Override
    public Node<K, V> allocate(boolean leaf) {
        Node<K, V> node = new Node<K, V>(
                leaf, new DefaultNodeId(), t);
        
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
