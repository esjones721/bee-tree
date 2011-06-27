package org.ardverk.btree;

import java.util.Iterator;

import org.ardverk.btree.NodeProvider.Intent;

public interface INode<K, V> {

    /**
     * 
     */
    public static interface Id {
    }
    
    public INode.Id getNodeId();
    
    public int getHeight();
    
    public int getNodeCount();
    
    public int getTupleCount();
    
    public boolean isEmpty();
    
    public boolean isOverflow();
    
    public boolean isUnderflow();
    
    public boolean isLeaf();
    
    public Tuple<K, V> firstTuple();
    
    public Tuple<K, V> lastTuple();
    
    public Tuple<K, V> firstTuple(NodeProvider<K, V> provider, Intent intent);
    
    public Tuple<K, V> lastTuple(NodeProvider<K, V> provider, Intent intent);
    
    public INode<K, V> firstNode(NodeProvider<K, V> provider, Intent intent);
    
    public INode<K, V> lastNode(NodeProvider<K, V> provider, Intent intent);
    
    public Tuple<K, V> get(NodeProvider<K, V> provider, K key);
    
    public Tuple<K, V> ceilingTuple(NodeProvider<K, V> provider, K key);
    
    public Tuple<K, V> put(NodeProvider<K, V> provider, K key, V value);
    
    public Tuple<K, V> remove(NodeProvider<K, V> provider, K key);
    
    public Median<K, V> split(NodeProvider<K, V> provider);
    
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider);
    
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider, 
            K key, boolean inclusive);
    
    /**
     * 
     */
    public static class Median<K, V> {
        
        private final Tuple<K, V> tuple;
        
        private final INode.Id nodeId;
        
        Median(Tuple<K, V> tuple, INode.Id nodeId) {
            this.tuple = tuple;
            this.nodeId = nodeId;
        }

        public K getKey() {
            return tuple.getKey();
        }
        
        public Tuple<K, V> getTuple() {
            return tuple;
        }

        public INode.Id getNodeId() {
            return nodeId;
        }
        
        @Override
        public String toString() {
            return "<" + tuple + ", " + nodeId + ">";
        }
    }
}
