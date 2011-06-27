package org.ardverk.btree;

import org.ardverk.btree.NodeProvider.Intent;

public abstract class AbstractNode<K, V> implements INode<K, V> {

    protected final INode.Id nodeId;
    
    protected final int height;
    
    protected final int t;
    
    public AbstractNode(INode.Id nodeId, int height, int t) {
        this.nodeId = nodeId;
        this.height = height;
        this.t = t;
    }
    
    @Override
    public INode.Id getNodeId() {
        return nodeId;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean isEmpty() {
        return getTupleCount() == 0;
    }

    @Override
    public boolean isOverflow() {
        return getTupleCount() >= 2*t-1;
    }

    @Override
    public boolean isUnderflow() {
        return getTupleCount() < t;
    }

    @Override
    public boolean isLeaf() {
        return getHeight() == 0;
    }
    
    @Override
    public Tuple<K, V> firstTuple(NodeProvider<K, V> provider, Intent intent) {
        INode<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstNode(provider, intent);
        }
        
        return node.firstTuple();
    }
    
    @Override
    public Tuple<K, V> lastTuple(NodeProvider<K, V> provider, Intent intent) {
        INode<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastNode(provider, intent);
        }
        
        return node.lastTuple();
    }
}
