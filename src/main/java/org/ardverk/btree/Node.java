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
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.ardverk.btree.NodeProvider.Intent;

public class Node<K, V> extends AbstractNode<K, V> {
    
    private final Bucket<Tuple<K, V>> tuples;
    
    private final Bucket<NodeId> children;
    
    public Node(NodeId nodeId, int height, int t) {
        this(nodeId, height, t, new Bucket<Tuple<K, V>>(2*t-1), 
                createBucket(height, 2*t));
    }
    
    public Node(NodeId nodeId, int height, int t, 
            Bucket<Tuple<K, V>> tuples, 
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
    
    public Bucket<Tuple<K, V>> getTuples() {
        return tuples;
    }
    
    public Bucket<NodeId> getChildren() {
        return children;
    }
    
    public int getTupleCount() {
        return tuples.size();
    }
    
    public int getChildCount() {
        return children != null ? children.size() : 0;
    }
    
    public Tuple<K, V> getTuple(int index) {
        return tuples.get(index);
    }
    
    private Tuple<K, V> removeTuple(int index) {
        return tuples.remove(index);
    }
    
    public Tuple<K, V> firstTuple() {
        return tuples.getFirst();
    }
    
    public Tuple<K, V> lastTuple() {
        return tuples.getLast();
    }
    
    private Tuple<K, V> removeLastTuple() {
        return tuples.removeLast();
    }
    
    public NodeId getChild(int index) {
        return children.get(index);
    }
    
    private NodeId removeChild(int index) {
        return children.remove(index);
    }
    
    private NodeId firstChild() {
        return children.getFirst();
    }
    
    private NodeId removeFirstChild() {
        return children.removeFirst();
    }
    
    private NodeId lastChild() {
        return children.getLast();
    }
    
    private NodeId removeLastChild() {
        return children.removeLast();
    }
    
    /**
     * Walk all the way to the left and return the first {@link Tuple}.
     */
    public Tuple<K, V> firstTuple(NodeProvider<K, V> provider, Intent intent) {
        return firstNode(provider, intent).firstTuple();
    }
    
    /**
     * Walk all the way to the right and return the last {@link Tuple}.
     */
    public Tuple<K, V> lastTuple(NodeProvider<K, V> provider, Intent intent) {
        return lastNode(provider, intent).lastTuple();
    }
    
    /**
     * Walks all the way to the left.
     */
    public Node<K, V> firstNode(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.firstChildNode(provider, intent);
        }
        
        return node;
    }
    
    /**
     * Walks all the way to the right.
     */
    public Node<K, V> lastNode(NodeProvider<K, V> provider, Intent intent) {
        Node<K, V> node = this;
        while (!node.isLeaf()) {
            node = node.lastChildNode(provider, intent);
        }
        
        return node;
    }
    
    public Node<K, V> firstChildNode(NodeProvider<K, V> provider, Intent intent) {
        NodeId first = firstChild();
        return provider.get(first, intent);
    }
    
    public Node<K, V> lastChildNode(NodeProvider<K, V> provider, Intent intent) {
        NodeId last = lastChild();
        return provider.get(last, intent);
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, 
            int index, Intent intent) {
        NodeId nodeId = getChild(index);
        return (Node<K, V>)provider.get(nodeId, intent);
    }
    
    private Tuple<K, V> setTuple(int index, Tuple<K, V> tuple) {
        return tuples.set(index, tuple);
    }
    
    public void addTuple(Tuple<K, V> tuple) {
        tuples.addLast(tuple);
    }
    
    public void addFirstTuple(Tuple<K, V> tuple) {
        tuples.addFirst(tuple);
    }
    
    private void addTuple(int index, Tuple<K, V> tuple) {
        tuples.add(index, tuple);
    }
    
    public void addChild(NodeId nodeId) {
        children.addLast(nodeId);
    }
    
    public void addFirstChild(NodeId nodeId) {
        children.addFirst(nodeId);
    }
    
    private void addChild(int index, NodeId nodeId) {
        children.add(index, nodeId);
    }
    
    public void addMedian(Median<K, V> median) {
        addMedian(getTupleCount(), median);
    }
    
    private void addMedian(int index, Median<K, V> median) {
        addTuple(index, median.getTuple());
        addChild(index+1, median.getChild());
    }
    
    private int binarySearch(NodeProvider<K, V> provider, K key) {
        Comparator<? super K> comparator = provider.comparator();
        return TupleUtils.binarySearch(tuples, key, comparator);
    }
    
    public Tuple<K, V> ceilingTuple(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        if (index >= 0 || isLeaf()) {
            if (index < 0) {
                index = -index - 1;
            }
            
            return getTuple(index);
        }
        
        Node<K, V> node = getNode(provider, -index - 1, Intent.READ);
        return node.ceilingTuple(provider, key);
    }
    
    public Tuple<K, V> get(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        // Found the Key?
        if (index >= 0) {
            return getTuple(index);
        }
        
        // I didn't find it but I know where to look for it!
        if (!isLeaf()) {
            Node<K, V> node = getNode(provider, -index - 1, Intent.READ);
            return node.get(provider, key);
        }
        return null;
    }
    
    public Tuple<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        int index = binarySearch(provider, key);
        
        // Replace an existing Key-Value
        if (index >= 0) {
            return setTuple(index, new Tuple<K, V>(key, value));
        }
        
        assert (index < 0);
        index = -index - 1;
        
        // Found a leaf where it should be stored!
        if (isLeaf()) {
            assert (!isOverflow());
            addTuple(index, new Tuple<K, V>(key, value));
            return null;
        }
        
        // Keep looking!
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isOverflow()) {
            
            Median<K, V> median = node.split(provider);
            addMedian(index, median);
            
            Comparator<? super K> comparator = provider.comparator();
            int cmp = comparator.compare(key, median.getKey());
            if (0 < cmp) {
                node = getNode(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public Tuple<K, V> remove(NodeProvider<K, V> provider, K key) {
        
        int index = binarySearch(provider, key);
        
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
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {
            fix(provider, node, index);
        }
        
        return node.remove(provider, key);
    }
    
    private Tuple<K, V> removeInternal(NodeProvider<K, V> provider, K key, int index) {
        Tuple<K, V> tuple = null;
        
        Node<K, V> left = getNode(provider, index, Intent.WRITE);
        if (!left.isUnderflow()) {
            Tuple<K, V> last = left.lastTuple(provider, Intent.WRITE);
            tuple = setTuple(index, last);
            left.remove(provider, last.getKey());
            
        } else {
            
            Node<K, V> right = getNode(provider, index+1, Intent.WRITE);
            if (!right.isUnderflow()) {
                Tuple<K, V> first = right.firstTuple(provider, Intent.WRITE);
                tuple = setTuple(index, first);
                right.remove(provider, first.getKey());
            } else {
                
                Tuple<K, V> median = removeTuple(index);
                removeChild(index+1);
                
                left.mergeWithRight(median, right);
                
                provider.free(right);
                
                tuple = left.remove(provider, key);
            }
        }
        
        return tuple;
    }
    
    private void fix(NodeProvider<K, V> provider, Node<K, V> node, int index) {
        
        Node<K, V> left = null;
        if (0 < index) {
            left = getNode(provider, index-1, Intent.WRITE);
        }
        
        // Borrow Entry from left sibling
        if (left != null && !left.isUnderflow()) {
            Tuple<K, V> last = left.removeLastTuple();
            Tuple<K, V> tuple = setTuple(index-1, last);
            node.addFirstTuple(tuple);
            
            if (!node.isLeaf()) {
                node.addFirstChild(left.removeLastChild());
            }
        } else {
            
            Node<K, V> right = null;
            if (index < getChildCount()-1) {
                right = getNode(provider, index+1, Intent.WRITE);
            }
            
            // Borrow tuple from right sibling
            if (right != null && !right.isUnderflow()) {
                Tuple<K, V> first = right.removeLastTuple();
                Tuple<K, V> tuple = setTuple(index, first);
                node.addTuple(tuple);
                
                if (!node.isLeaf()) {
                    node.addChild(right.removeFirstChild());
                }
            
            // Neither sibling has enough Entries! Merge them!
            } else {
                
                if (left != null && !left.isEmpty()) {
                    Tuple<K, V> median = removeTuple(index-1);
                    removeChild(index-1);
                    
                    node.mergeWithLeft(median, left);
                    
                    provider.free(left);
                } else {
                    
                    Tuple<K, V> median = removeTuple(index);
                    removeChild(index);
                    
                    node.mergeWithRight(median, right);
                    
                    provider.free(right);
                }
            }
        }
    }
    
    private void mergeWithRight(Tuple<K, V> median, Node<K, V> right) {
        tuples.addLast(median);
        tuples.addAll(right.tuples);
        if (!isLeaf()) {
            children.addAll(right.children);
        }
    }
    
    private void mergeWithLeft(Tuple<K, V> median, Node<K, V> left) {
        tuples.addFirst(median);
        tuples.addAll(0, left.tuples);
        if (!isLeaf()) {
            children.addAll(0, left.children);
        }
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int tupleCount = getTupleCount();
        int m = tupleCount/2;
        
        Node<K, V> dst = provider.allocate(height);
        
        Tuple<K, V> median = removeTuple(m);
        
        if (!leaf) {
            NodeId nodeId = removeChild(m+1);
            dst.addChild(nodeId);
        }
        
        while (m < getTupleCount()) {
            dst.addTuple(removeTuple(m));
            
            if (!leaf) {
                dst.addChild(removeChild(m+1));
            }
        }
        
        return new Median<K, V>(median, dst.getId());
    }
    
    public Node<K, V> copy(NodeProvider<K, V> provider) {
        boolean leaf = isLeaf();
        Node<K, V> dst = provider.allocate(height);
        
        dst.tuples.addAll(tuples);
        
        if (!leaf) {
            dst.children.addAll(children);
        }
        
        return dst;
    }
    
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider) {
        Deque<Index> stack = new ArrayDeque<Index>();
        
        stack.push(new Index(getId(), 0));
        walk(provider, this, stack.peek(), stack);
        
        return new NodeIterator<K, V>(provider, stack);
    }
    
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider, 
            K key, boolean inclusive) {
        return iterator(provider, key, inclusive, new ArrayDeque<Index>());
    }
    
    private Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider, 
            K key, boolean inclusive, Deque<Index> stack) {
        
        int index = binarySearch(provider, key);
        
        int path = (index < 0 ? -index - 1 : index);
        stack.push(new Index(getId(), path));
        
        if (index >= 0 || isLeaf()) {
            
            if (!inclusive && index >= 0) {
                stack.peek().next();
            }
            
            return new NodeIterator<K, V>(provider, stack);
        }
        
        Node<K, V> node = getNode(provider, path, Intent.READ);
        return node.iterator(provider, key, inclusive, stack);
    }
    
    private static <K, V> Index walk(NodeProvider<K, V> provider, 
            Node<K, V> node, Index index, Deque<Index> stack) {
        
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
    
    private static Bucket<NodeId> createBucket(int height, int maxSize) {
        if (0 < height) {
            return new Bucket<NodeId>(maxSize);
        }
        return null;
    }
    
    /**
     * 
     */
    public static class Median<K, V> {
        
        private final Tuple<K, V> tuple;
        
        private final NodeId nodeId;
        
        private Median(Tuple<K, V> tuple, NodeId nodeId) {
            this.tuple = tuple;
            this.nodeId = nodeId;
        }

        public K getKey() {
            return tuple.getKey();
        }
        
        public Tuple<K, V> getTuple() {
            return tuple;
        }

        public NodeId getChild() {
            return nodeId;
        }
        
        @Override
        public String toString() {
            return "<" + tuple + ", " + nodeId + ">";
        }
    }
    
    private static class NodeIterator<K, V> implements Iterator<Tuple<K, V>> {

        private final NodeProvider<K, V> provider;
        
        private final Deque<Index> stack;
        
        private Index index = null;
        
        private Node<K, V> node = null;
        
        private Tuple<K, V> next = null;
        
        public NodeIterator(NodeProvider<K, V> provider, Deque<Index> stack) {
            this.provider = provider;
            this.stack = stack;
            
            if (!stack.isEmpty()) {
                index = stack.poll();
                node = (Node<K, V>)provider.get(index.getNodeId(), Intent.READ);
                
                next = nextEntry();
            }
        }
        
        private Tuple<K, V> nextEntry() {
            
            if (index.hasNext(node)) {
                return index.next(node);
            }
            
            if (!stack.isEmpty()) {
                index = stack.peek();
                node = (Node<K, V>)provider.get(index.getNodeId(), Intent.READ);
                assert (!node.isLeaf());
                
                if (index.hasNext(node)) {
                    Tuple<K, V> next = index.next(node);
                    
                    index = Node.walk(provider, node, index, stack);
                    node = (Node<K, V>)provider.get(index.getNodeId(), Intent.READ);
                    
                    return next;
                }
                
                stack.pop();
                return nextEntry();
            }
            
            return null;
        }
        
        @Override
        public boolean hasNext() {
            return next != null;
        }
        
        @Override
        public Tuple<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            try {
                return next;
            } finally {
                next = nextEntry();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
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
        
        public boolean hasNext(Node<?, ?> node) {
            return index < node.getTupleCount();
        }
        
        public <K, V> Tuple<K, V> next(Node<K, V> node) {
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
