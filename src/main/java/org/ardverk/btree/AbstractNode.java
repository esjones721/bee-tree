package org.ardverk.btree;

abstract class AbstractNode<K, V> implements NodeId {

    protected final NodeId nodeId;
    
    protected final int height;
    
    protected final int t;
    
    public AbstractNode(NodeId nodeId, int height, int t) {
        this.nodeId = nodeId;
        this.height = height;
        this.t = t;
    }
    
    @Override
    public NodeId getId() {
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
    
    public abstract int getChildCount();
}
