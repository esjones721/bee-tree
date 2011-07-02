/*
 * Copyright 2011 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.btree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

import org.ardverk.btree.NodeProvider.Intent;

public class Node extends AbstractNode implements NodeLock {
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private final Bucket<Tuple> tuples;
    
    private final Bucket<NodeId> children;
    
    public Node(NodeId nodeId, int height, int t) {
        this(nodeId, height, t, new Bucket<Tuple>(2*t-1), 
                createBucket(height, 2*t));
    }
    
    public Node(NodeId nodeId, int height, int t, 
            Bucket<Tuple> tuples, 
            Bucket<NodeId> children) {
        super(nodeId, height, t);
        
        this.tuples = tuples;
        
        if (height == 0) {
            children = null;
        }
        
        this.children = children;
        
        assert (tuples.getMaxSize() == 2*t-1);
        assert (children == null || children.getMaxSize() == 2*t);
    }
    
    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    public Bucket<Tuple> getTuples() {
        return tuples;
    }
    
    public Bucket<NodeId> getNodes() {
        return children;
    }
    
    public int getTupleCount() {
        return tuples.size();
    }
    
    public int getNodeCount() {
        return children != null ? children.size() : 0;
    }
    
    public Tuple getTuple(int index) {
        return tuples.get(index);
    }
    
    private Tuple removeTuple(int index) {
        return tuples.remove(index);
    }
    
    public Tuple firstTuple() {
        return tuples.getFirst();
    }
    
    public Tuple lastTuple() {
        return tuples.getLast();
    }
    
    private Tuple removeFirstTuple() {
        return tuples.removeFirst();
    }
    
    private Tuple removeLastTuple() {
        return tuples.removeLast();
    }
    
    public NodeId getNode(int index) {
        return children.get(index);
    }
    
    private NodeId removeNode(int index) {
        return children.remove(index);
    }
    
    private NodeId firstNode() {
        return children.getFirst();
    }
    
    private NodeId removeFirstNode() {
        return children.removeFirst();
    }
    
    private NodeId lastNode() {
        return children.getLast();
    }
    
    private NodeId removeLastNode() {
        return children.removeLast();
    }
    
    /**
     * Walk all the way to the left and return the first {@link Tuple}.
     */
    public Tuple firstTuple(NodeProvider provider, Intent intent) {
        return firstNode(provider, intent).firstTuple();
    }
    
    /**
     * Walk all the way to the right and return the last {@link Tuple}.
     */
    public Tuple lastTuple(NodeProvider provider, Intent intent) {
        return lastNode(provider, intent).lastTuple();
    }
    
    /**
     * Walks all the way to the left.
     */
    public Node firstNode(NodeProvider provider, Intent intent) {
        Node node = this;
        while (!node.isLeaf()) {
            node = node.firstChildNode(provider, intent);
        }
        
        return node;
    }
    
    /**
     * Walks all the way to the right.
     */
    public Node lastNode(NodeProvider provider, Intent intent) {
        Node node = this;
        while (!node.isLeaf()) {
            node = node.lastChildNode(provider, intent);
        }
        
        return node;
    }
    
    public Node firstChildNode(NodeProvider provider, Intent intent) {
        NodeId first = firstNode();
        return provider.get(first, intent);
    }
    
    public Node lastChildNode(NodeProvider provider, Intent intent) {
        NodeId last = lastNode();
        return provider.get(last, intent);
    }
    
    private Node getNode(NodeProvider provider, 
            int index, Intent intent) {
        NodeId nodeId = getNode(index);
        return provider.get(nodeId, intent);
    }
    
    private Tuple setTuple(int index, Tuple tuple) {
        return tuples.set(index, tuple);
    }
    
    public void addTuple(Tuple tuple) {
        tuples.addLast(tuple);
    }
    
    public void addFirstTuple(Tuple tuple) {
        tuples.addFirst(tuple);
    }
    
    private void addTuple(int index, Tuple tuple) {
        tuples.add(index, tuple);
    }
    
    public void addNode(NodeId nodeId) {
        children.addLast(nodeId);
    }
    
    public void addFirstNode(NodeId nodeId) {
        children.addFirst(nodeId);
    }
    
    private void addNode(int index, NodeId nodeId) {
        children.add(index, nodeId);
    }
    
    public void addTupleNode(TupleNode median) {
        addTupleNode(getTupleCount(), median);
    }
    
    private void addTupleNode(int index, TupleNode median) {
        addTuple(index, median.getTuple());
        addNode(index+1, median.getNode());
    }
    
    private int binarySearch(byte[] key) {
        return TupleUtils.binarySearch(tuples, key);
    }
    
    public Tuple ceilingTuple(NodeProvider provider, byte[] key) {
        int index = binarySearch(key);
        
        if (index >= 0 || isLeaf()) {
            if (index < 0) {
                index = -index - 1;
            }
            
            return getTuple(index);
        }
        
        Node node = getNode(provider, -index - 1, Intent.READ);
        return node.ceilingTuple(provider, key);
    }
    
    public Tuple get(NodeProvider provider, byte[] key) {
        int index = binarySearch(key);
        
        // Found the Key?
        if (index >= 0) {
            return getTuple(index);
        }
        
        // I didn't find it but I know where to look for it!
        if (!isLeaf()) {
            Node node = getNode(provider, -index - 1, Intent.READ);
            return node.get(provider, key);
        }
        return null;
    }
    
    public Tuple put(NodeProvider provider, NodeLock parent, byte[] key, byte[] value) {
        int index = binarySearch(key);
        
        // Replace an existing Key-Value
        if (index >= 0) {
            try {
                return setTuple(index, new Tuple(key, value));
            } finally {
                unlock();
                parent.unlock();
            }
        }
        
        assert (index < 0);
        index = -index - 1;
        
        // Found a leaf where it should be stored!
        if (isLeaf()) {
            try {
                assert (!isOverflow());
                addTuple(index, new Tuple(key, value));
                return null;
            } finally {
                unlock();
                parent.unlock();
            }
        }
        
        // Keep looking!
        
        Node node = getNode(provider, index, Intent.WRITE);
        
        boolean success = false;
        
        node.lock();
        try {
            if (node.isOverflow()) {
                TupleNode median = node.split(provider);
                addTupleNode(index, median);
                
                int cmp = TupleUtils.compare(key, median.getKey());
                if (0 < cmp) {
                    node.unlock();
                    
                    node = getNode(provider, index + 1, Intent.WRITE);
                    node.lock();
                }
            }
            
            parent.unlock();
            
            Tuple tuple = node.put(provider, this, key, value);
            success = true;
            return tuple;
            
        } finally {
            if (!success) {
                node.unlock();
            }
        }
    }
    
    public Tuple remove(NodeProvider provider, byte[] key) {
        
        int index = binarySearch(key);
        
        // It must be here if it's a leaf!
        if (isLeaf()) {
            if (index >= 0) {
                return removeTuple(index);
            }
            return null;
        }
        
        // Found the Key-Value in an internal Node!
        if (index >= 0) {
            return removeInternal(provider, key, index);
        } 
        
        // Keep looking
        assert (index < 0);
        index = -index - 1;
        
        Node node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {
            fix(provider, node, index);
        }
        
        return node.remove(provider, key);
    }
    
    private Tuple removeInternal(NodeProvider provider, byte[] key, int index) {
        Tuple tuple = null;
        
        Node left = getNode(provider, index, Intent.WRITE);
        if (!left.isUnderflow()) {
            Tuple last = left.lastTuple(provider, Intent.WRITE);
            tuple = setTuple(index, last);
            left.remove(provider, last.getKey());
            
        } else {
            
            Node right = getNode(provider, index+1, Intent.WRITE);
            if (!right.isUnderflow()) {
                Tuple first = right.firstTuple(provider, Intent.WRITE);
                tuple = setTuple(index, first);
                right.remove(provider, first.getKey());
            } else {
                
                Tuple median = removeTuple(index);
                removeNode(index+1);
                
                left.mergeWithRight(median, right);
                
                provider.free(right);
                
                tuple = left.remove(provider, key);
            }
        }
        
        return tuple;
    }
    
    private void fix(NodeProvider provider, Node node, int index) {
        
        Node left = null;
        if (0 < index) {
            left = getNode(provider, index-1, Intent.WRITE);
        }
        
        // Borrow Entry from left sibling
        if (left != null && !left.isUnderflow()) {
            Tuple last = left.removeLastTuple();
            Tuple tuple = setTuple(index-1, last);
            node.addFirstTuple(tuple);
            
            if (!node.isLeaf()) {
                node.addFirstNode(left.removeLastNode());
            }
        } else {
            
            Node right = null;
            if (index < getNodeCount()-1) {
                right = getNode(provider, index+1, Intent.WRITE);
            }
            
            // Borrow tuple from right sibling
            if (right != null && !right.isUnderflow()) {
                Tuple first = right.removeFirstTuple();
                Tuple tuple = setTuple(index, first);
                node.addTuple(tuple);
                
                if (!node.isLeaf()) {
                    node.addNode(right.removeFirstNode());
                }
            
            // Neither sibling has enough Entries! Merge them!
            } else {
                
                if (left != null && !left.isEmpty()) {
                    Tuple median = removeTuple(index-1);
                    removeNode(index-1);
                    
                    node.mergeWithLeft(median, left);
                    
                    provider.free(left);
                } else {
                    
                    Tuple median = removeTuple(index);
                    removeNode(index+1);
                    
                    node.mergeWithRight(median, right);
                    
                    provider.free(right);
                }
            }
        }
    }
    
    private void mergeWithRight(Tuple median, Node right) {
        tuples.addLast(median);
        tuples.addAll(right.tuples);
        if (!isLeaf()) {
            children.addAll(right.children);
        }
    }
    
    private void mergeWithLeft(Tuple median, Node left) {
        tuples.addFirst(median);
        tuples.addAll(0, left.tuples);
        if (!isLeaf()) {
            children.addAll(0, left.children);
        }
    }
    
    public TupleNode split(NodeProvider provider) {
        
        boolean leaf = isLeaf();
        
        int tupleCount = getTupleCount();
        int m = tupleCount/2;
        
        Node dst = provider.allocate(height);
        
        Tuple median = removeTuple(m);
        
        if (!leaf) {
            NodeId nodeId = removeNode(m+1);
            dst.addNode(nodeId);
        }
        
        while (m < getTupleCount()) {
            dst.addTuple(removeTuple(m));
            
            if (!leaf) {
                dst.addNode(removeNode(m+1));
            }
        }
        
        return new TupleNode(median, dst.getId());
    }
    
    public Node copy(NodeProvider provider) {
        boolean leaf = isLeaf();
        Node dst = provider.allocate(height);
        
        dst.tuples.addAll(tuples);
        
        if (!leaf) {
            dst.children.addAll(children);
        }
        
        return dst;
    }
    
    public Iterator<Tuple> iterator(NodeProvider provider) {
        Deque<Index> stack = new ArrayDeque<Index>();
        
        stack.push(new Index(getId(), 0));
        walk(provider, this, stack.peek(), stack);
        
        return new NodeIterator(provider, stack);
    }
    
    public Iterator<Tuple> iterator(NodeProvider provider, 
            byte[] key, boolean inclusive) {
        return iterator(provider, key, inclusive, new ArrayDeque<Index>());
    }
    
    private Iterator<Tuple> iterator(NodeProvider provider, 
            byte[] key, boolean inclusive, Deque<Index> stack) {
        
        int index = binarySearch(key);
        
        int path = (index < 0 ? -index - 1 : index);
        stack.push(new Index(getId(), path));
        
        if (index >= 0 || isLeaf()) {
            
            if (!inclusive && index >= 0) {
                stack.peek().next();
            }
            
            return new NodeIterator(provider, stack);
        }
        
        Node node = getNode(provider, path, Intent.READ);
        return node.iterator(provider, key, inclusive, stack);
    }
    
    private static Index walk(NodeProvider provider, 
            Node node, Index index, Deque<Index> stack) {
        
        while (!node.isLeaf()) {
            node = node.getNode(provider, index.get(), Intent.READ);
            index = new Index(node.getId(), 0);
            stack.push(index);
        }
        
        return index;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tuples);
        
        if (children != null) {
            sb.append(children);
        } else {
            sb.append("[]");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates and returns a {@link Bucket} of the given max size
     * if the height is greater than zero.
     */
    private static Bucket<NodeId> createBucket(int height, int maxSize) {
        if (0 < height) {
            return new Bucket<NodeId>(maxSize);
        }
        return null;
    }
    
    /**
     * A {@link TupleNode} is simply a pair of a {@link Tuple} and a 
     * {@link NodeId}. It's returned by {@link Node#split(NodeProvider)} 
     * and is the median element that needs to move up to its parent 
     * {@link Node}.
     */
    public static class TupleNode {
        
        private final Tuple tuple;
        
        private final NodeId nodeId;
        
        private TupleNode(Tuple tuple, NodeId nodeId) {
            this.tuple = tuple;
            this.nodeId = nodeId;
        }

        byte[] getKey() {
            return tuple.getKey();
        }
        
        public Tuple getTuple() {
            return tuple;
        }

        public NodeId getNode() {
            return nodeId;
        }
        
        @Override
        public String toString() {
            return "<" + tuple + ", " + nodeId + ">";
        }
    }
    
    /**
     * An {@link Iterator} that iterates over a B-Tree.
     */
    private static class NodeIterator implements Iterator<Tuple> {

        private final NodeProvider provider;
        
        private final Deque<Index> stack;
        
        private Index index = null;
        
        private Node node = null;
        
        private Tuple next = null;
        
        public NodeIterator(NodeProvider provider, Deque<Index> stack) {
            this.provider = provider;
            this.stack = stack;
            
            if (!stack.isEmpty()) {
                index = stack.poll();
                node = provider.get(index.getNodeId(), Intent.READ);
                
                next = nextTuple();
            }
        }
        
        private Tuple nextTuple() {
            
            if (index.hasNext(node)) {
                return index.next(node);
            }
            
            if (!stack.isEmpty()) {
                index = stack.peek();
                node = provider.get(index.getNodeId(), Intent.READ);
                assert (!node.isLeaf());
                
                if (index.hasNext(node)) {
                    Tuple next = index.next(node);
                    
                    index = Node.walk(provider, node, index, stack);
                    node = provider.get(index.getNodeId(), Intent.READ);
                    
                    return next;
                }
                
                stack.pop();
                return nextTuple();
            }
            
            return null; // EOF
        }
        
        @Override
        public boolean hasNext() {
            return next != null;
        }
        
        @Override
        public Tuple next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            try {
                return next;
            } finally {
                next = nextTuple();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * The {@link Index} is used by {@link NodeIterator} to keep track 
     * of the current {@link Node} and the current {@link Tuple}.
     */
    private static class Index {
        
        private final NodeId nodeId;
        
        private int index;
        
        private Index(NodeId nodeId, int index) {
            this.nodeId = nodeId;
            this.index = index;
        }
        
        public NodeId getNodeId() {
            return nodeId;
        }
        
        public boolean hasNext(Node node) {
            return index < node.getTupleCount();
        }
        
        public Tuple next(Node node) {
            return node.getTuple(index++);
        }
        
        public int get() {
            return index;
        }
        
        public int next() {
            return index++;
        }
        
        @Override
        public String toString() {
            return "<" + nodeId + ", " + index + ">";
        }
    }
}
