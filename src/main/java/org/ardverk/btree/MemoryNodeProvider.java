package org.ardverk.btree;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryNodeProvider implements NodeProvider {

    private final Map<NodeId, Node> nodes = new HashMap<NodeId, Node>();
    
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
        //System.out.println("FREE: " + node);
        //Thread.dumpStack();
        nodes.remove(node.getId());
    }

    @Override
    public Node get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
    
    @Override
    public String toString() {
        return root + ", " + nodes;
    }
    
    private static class IntegerId implements NodeId {
        
        private static final AtomicInteger COUNTER = new AtomicInteger();
        
        private final int value = COUNTER.incrementAndGet();
        
        @Override
        public int hashCode() {
            return value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof IntegerId)) {
                return false;
            }
            
            IntegerId other = (IntegerId)o;
            return value == other.value;
        }
        
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}
