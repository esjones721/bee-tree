package org.ardverk.btree;

abstract class AbstractNode<K, V> {

    protected final NodeId nodeId;
    
    protected final int height;
    
    protected final int t;
    
    public AbstractNode(NodeId nodeId, int height, int t) {
        this.nodeId = nodeId;
        this.height = height;
        this.t = t;
    }
    
    public NodeId getNodeId() {
        return nodeId;
    }

    public int getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return getTupleCount() == 0;
    }

    public boolean isOverflow() {
        return getTupleCount() >= 2*t-1;
    }

    public boolean isUnderflow() {
        return getTupleCount() < t;
    }

    public boolean isLeaf() {
        return getHeight() == 0;
    }
    
    public abstract int getTupleCount();
    
    public abstract int getNodeCount();
    
    /**
     * 
     */
    public static class Median<K, V> {
        
        private final Tuple<K, V> tuple;
        
        private final NodeId nodeId;
        
        Median(Tuple<K, V> tuple, NodeId nodeId) {
            this.tuple = tuple;
            this.nodeId = nodeId;
        }

        public K getKey() {
            return tuple.getKey();
        }
        
        public Tuple<K, V> getTuple() {
            return tuple;
        }

        public NodeId getNodeId() {
            return nodeId;
        }
        
        @Override
        public String toString() {
            return "<" + tuple + ", " + nodeId + ">";
        }
    }
}
