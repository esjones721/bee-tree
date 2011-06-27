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
    
    private final Bucket<INode.Id> nodes;
    
    public Node(INode.Id nodeId, int height, int t) {
        this(nodeId, height, t, new Bucket<Tuple<K, V>>(2*t-1), 
                createBucket(height, 2*t));
    }
    
    public Node(INode.Id nodeId, int height, int t, 
            Bucket<Tuple<K, V>> tuples, 
            Bucket<INode.Id> nodes) {
        super(nodeId, height, t);
        
        this.tuples = tuples;
        
        if (height == 0) {
            nodes = null;
        }
        
        this.nodes = nodes;
        
        assert (tuples.getMaxSize() == 2*t-1);
        assert (nodes == null || nodes.getMaxSize() == 2*t);
    }
    
    public Bucket<Tuple<K, V>> getTuples() {
        return tuples;
    }
    
    public Bucket<INode.Id> getNodeIds() {
        return nodes;
    }
    
    public int getNodeCount() {
        return nodes != null ? nodes.size() : 0;
    }
    
    public int getTupleCount() {
        return tuples.size();
    }
    
    public Tuple<K, V> getTuple(int index) {
        return tuples.get(index);
    }
    
    private Tuple<K, V> removeTuple(int index) {
        return tuples.remove(index);
    }
    
    @Override
    public Tuple<K, V> firstTuple() {
        return tuples.getFirst();
    }
    
    @Override
    public Tuple<K, V> lastTuple() {
        return tuples.getLast();
    }
    
    private Tuple<K, V> removeLastTuple() {
        return tuples.removeLast();
    }
    
    public INode.Id getNodeId(int index) {
        return nodes.get(index);
    }
    
    private INode.Id removeNodeId(int index) {
        return nodes.remove(index);
    }
    
    private INode.Id firstNodeId() {
        return nodes.getFirst();
    }
    
    private INode.Id removeFirstNodeId() {
        return nodes.removeFirst();
    }
    
    private INode.Id lastNodeId() {
        return nodes.getLast();
    }
    
    private INode.Id removeLastNodeId() {
        return nodes.removeLast();
    }
    
    @Override
    public INode<K, V> firstNode(NodeProvider<K, V> provider, Intent intent) {
        INode.Id first = firstNodeId();
        return provider.get(first, intent);
    }
    
    @Override
    public INode<K, V> lastNode(NodeProvider<K, V> provider, Intent intent) {
        INode.Id last = lastNodeId();
        return provider.get(last, intent);
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, 
            int index, Intent intent) {
        INode.Id nodeId = getNodeId(index);
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
    
    public void addNodeId(INode.Id nodeId) {
        nodes.addLast(nodeId);
    }
    
    public void addFirstNodeId(INode.Id nodeId) {
        nodes.addFirst(nodeId);
    }
    
    private void addNodeId(int index, INode.Id nodeId) {
        nodes.add(index, nodeId);
    }
    
    public void addMedian(Median<K, V> median) {
        addMedian(getTupleCount(), median);
    }
    
    private void addMedian(int index, Median<K, V> median) {
        addTuple(index, median.getTuple());
        addNodeId(index+1, median.getNodeId());
    }
    
    private int binarySearch(NodeProvider<K, V> provider, K key) {
        Comparator<? super K> comparator = provider.comparator();
        return TupleUtils.binarySearch(tuples, key, comparator);
    }
    
    @Override
    public Tuple<K, V> ceilingTuple(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        if (index >= 0 || isLeaf()) {
            if (index < 0) {
                index = -index - 1;
            }
            
            return getTuple(index);
        }
        
        INode<K, V> node = getNode(provider, -index - 1, Intent.READ);
        return node.ceilingTuple(provider, key);
    }
    
    @Override
    public Tuple<K, V> get(NodeProvider<K, V> provider, K key) {
        int index = binarySearch(provider, key);
        
        // Found the Key?
        if (index >= 0) {
            return getTuple(index);
        }
        
        // I didn't find it but I know where to look for it!
        if (!isLeaf()) {
            INode<K, V> node = getNode(provider, -index - 1, Intent.READ);
            return node.get(provider, key);
        }
        return null;
    }
    
    @Override
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
        INode<K, V> node = getNode(provider, index, Intent.WRITE);
        
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
    
    @Override
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
        
        INode<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isUnderflow()) {
            fix(provider, node, index);
        }
        
        return node.remove(provider, key);
    }
    
    private Tuple<K, V> removeInternal(NodeProvider<K, V> provider, K key, int index) {
        Tuple<K, V> tuple = null;
        
        INode<K, V> left = getNode(provider, index, Intent.WRITE);
        if (!left.isUnderflow()) {
            Tuple<K, V> last = left.lastTuple(provider, Intent.WRITE);
            tuple = setTuple(index, last);
            left.remove(provider, last.getKey());
            
        } else {
            
            INode<K, V> right = getNode(provider, index+1, Intent.WRITE);
            if (!right.isUnderflow()) {
                Tuple<K, V> first = right.firstTuple(provider, Intent.WRITE);
                tuple = setTuple(index, first);
                right.remove(provider, first.getKey());
            } else {
                
                Tuple<K, V> median = removeTuple(index);
                removeNodeId(index+1);
                
                left.mergeWithRight(median, right);
                
                provider.free(right);
                
                tuple = left.remove(provider, key);
            }
        }
        
        return tuple;
    }
    
    private void fix(NodeProvider<K, V> provider, INode<K, V> node, int index) {
        
        INode<K, V> left = null;
        if (0 < index) {
            left = getNode(provider, index-1, Intent.WRITE);
        }
        
        // Borrow Entry from left sibling
        if (left != null && !left.isUnderflow()) {
            Tuple<K, V> last = left.removeLastTuple();
            Tuple<K, V> tuple = setTuple(index-1, last);
            node.addFirstTuple(tuple);
            
            if (!node.isLeaf()) {
                node.addFirstNodeId(left.removeLastNodeId());
            }
        } else {
            
            INode<K, V> right = null;
            if (index < getNodeCount()-1) {
                right = getNode(provider, index+1, Intent.WRITE);
            }
            
            // Borrow tuple from right sibling
            if (right != null && !right.isUnderflow()) {
                Tuple<K, V> first = right.removeLastTuple();
                Tuple<K, V> tuple = setTuple(index, first);
                node.addTuple(tuple);
                
                if (!node.isLeaf()) {
                    node.addNodeId(right.removeFirstNodeId());
                }
            
            // Neither sibling has enough Entries! Merge them!
            } else {
                
                if (left != null && !left.isEmpty()) {
                    Tuple<K, V> median = removeTuple(index-1);
                    removeNodeId(index-1);
                    
                    node.mergeWithLeft(median, left);
                    
                    provider.free(left);
                } else {
                    
                    Tuple<K, V> median = removeTuple(index);
                    removeNodeId(index);
                    
                    node.mergeWithRight(median, right);
                    
                    provider.free(right);
                }
            }
        }
    }
    
    private void mergeWithRight(Tuple<K, V> median, INode<K, V> right) {
        tuples.addLast(median);
        tuples.addAll(right.tuples);
        if (!isLeaf()) {
            nodes.addAll(right.nodes);
        }
    }
    
    private void mergeWithLeft(Tuple<K, V> median, INode<K, V> left) {
        tuples.addFirst(median);
        tuples.addAll(0, left.tuples);
        if (!isLeaf()) {
            nodes.addAll(0, left.nodes);
        }
    }
    
    @Override
    public Median<K, V> split(NodeProvider<K, V> provider) {
        
        boolean leaf = isLeaf();
        
        int tupleCount = getTupleCount();
        int m = tupleCount/2;
        
        INode<K, V> dst = provider.allocate(height);
        
        Tuple<K, V> median = removeTuple(m);
        
        if (!leaf) {
            INode.Id nodeId = removeNodeId(m+1);
            dst.addNodeId(nodeId);
        }
        
        while (m < getTupleCount()) {
            dst.addTuple(removeTuple(m));
            
            if (!leaf) {
                dst.addNodeId(removeNodeId(m+1));
            }
        }
        
        return new Median<K, V>(median, dst.getNodeId());
    }
    
    public Node<K, V> copy(NodeProvider<K, V> provider) {
        boolean leaf = isLeaf();
        INode<K, V> dst = provider.allocate(height);
        
        dst.tuples.addAll(tuples);
        
        if (!leaf) {
            dst.nodes.addAll(nodes);
        }
        
        return dst;
    }
    
    @Override
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider) {
        Deque<Index> stack = new ArrayDeque<Index>();
        
        stack.push(new Index(getNodeId(), 0));
        walk(provider, this, stack.peek(), stack);
        
        return new NodeIterator<K, V>(provider, stack);
    }
    
    @Override
    public Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider, 
            K key, boolean inclusive) {
        return iterator(provider, key, inclusive, new ArrayDeque<Index>());
    }
    
    private Iterator<Tuple<K, V>> iterator(NodeProvider<K, V> provider, 
            K key, boolean inclusive, Deque<Index> stack) {
        
        int index = binarySearch(provider, key);
        
        int path = (index < 0 ? -index - 1 : index);
        stack.push(new Index(getNodeId(), path));
        
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
            index = new Index(node.getNodeId(), 0);
            stack.push(index);
        }
        
        return index;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tuples);
        
        if (nodes != null) {
            sb.append(nodes);
        } else {
            sb.append("[]");
        }
        
        return sb.toString();
    }
    
    private static Bucket<INode.Id> createBucket(int height, int maxSize) {
        if (0 < height) {
            return new Bucket<INode.Id>(maxSize);
        }
        return null;
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
        
        private final INode.Id nodeId;
        
        private int index;
        
        private Index(INode.Id nodeId, int index) {
            this.nodeId = nodeId;
            this.index = index;
        }
        
        public INode.Id getNodeId() {
            return nodeId;
        }
        
        public boolean hasNext(INode<?, ?> node) {
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
