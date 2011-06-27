package org.ardverk.btree.memory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.AbstractNodeProvider;
import org.ardverk.btree.Node;

public class DefaultNodeProvider<K, V> extends AbstractNodeProvider<K, V> {

    public final Map<Node.Id, Node<K, V>> nodes 
        = new LinkedHashMap<Node.Id, Node<K, V>>();
    
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
    public Node<K, V> get(Node.Id nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
    
    @Override
    public Node<K, V> allocate(boolean leaf) {
        Node<K, V> node = new Node<K, V>(
                new IntegerId(), leaf, t);
        
        nodes.put(node.getId(), node);
        return node;
    }

    @Override
    public void free(Node<? extends K, ? extends V> node) {
        nodes.remove(node.getId());
    }
    
    @Override
    public String toString() {
        return nodes.toString();
    }
}
