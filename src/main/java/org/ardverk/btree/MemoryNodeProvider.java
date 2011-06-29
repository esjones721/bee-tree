package org.ardverk.btree;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryNodeProvider implements NodeProvider {

    private final Map<NodeId, Node> nodes 
            = new LinkedHashMap<NodeId, Node>();
    
    private final int t;
    
    private final RootNode root;
    
    public MemoryNodeProvider(int t) {
        this.t = t;
        
        Node node = allocate(0);
        root = new RootNode(this, node, 0);
    }
    
    @Override
    public RootNode getRoot() {
        return root;
    }

    @Override
    public Node allocate(int height) {
        IntegerId nodeId = new IntegerId();
        Node node = new Node(nodeId, height, t);
        nodes.put(nodeId, node);
        return node;
    }

    @Override
    public void free(Node node) {
        nodes.remove(node.getId());
    }

    @Override
    public Node get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
}
