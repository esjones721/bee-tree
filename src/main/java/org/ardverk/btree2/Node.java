package org.ardverk.btree2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree2.NodeProvider.Intent;

public class Node<K, V> {

    private static final int t = 1;
    
    private final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(2*t);
    
    private final List<Id> nodes = new ArrayList<Id>(2*t+1);
    
    private final Id nodeId;
    
    private final boolean leaf;
    
    public Node(boolean leaf) {
        this(new Id(), leaf);
    }
    
    public Node(Id nodeId, boolean leaf) {
        this.nodeId = nodeId;
        this.leaf = leaf;
    }
    
    public Id getId() {
        return nodeId;
    }
    
    public int size() {
        return entries.size();
    }
    
    public boolean isEmpty() {
        return entries.isEmpty();
    }
    
    public boolean isFull() {
        return size() >= 2*t;
    }
    
    public void add(Entry<K, V> entry) {
        entries.add(entry);
    }
    
    public void add(Id nodeId) {
        nodes.add(nodeId);
    }
    
    public Entry<K, V> get(NodeProvider<K, V> provider, K key) {
        if (!isEmpty()) {
            Comparator<? super K> comparator = provider.comparator();
            int index = EntryUtils.binarySearch(entries, key, comparator);
            
            // Found the Key?
            if (index >= 0) {
                return entries.get(index);
            }
            
            // Didn't find it but know where to look for it!
            if (!leaf) {
                return getEntry(provider, key, -index - 1, Intent.READ);
            }
        }
        return null;
    }
    
    private Node<K, V> getNode(NodeProvider<K, V> provider, int index, Intent intent) {
        Id nodeId = nodes.get(index);
        return provider.get(nodeId, intent);
    }
    
    private Entry<K, V> getEntry(NodeProvider<K, V> provider, 
            K key, int index, Intent intent) {
        Node<K, V> node = getNode(provider, index, intent);
        return node.get(provider, key);
    }
    
    public Entry<K, V> put(NodeProvider<K, V> provider, K key, V value) {
        Comparator<? super K> comparator = provider.comparator();
        int index = EntryUtils.binarySearch(entries, key, comparator);
        
        if (index >= 0) {
            return entries.set(index, new Entry<K, V>(key, value));
        }
        
        assert (index < 0);
        index = -index - 1;
        
        if (leaf) {
            assert (!isFull());
            entries.add(index, new Entry<K, V>(key, value));
            return null;
        }
        
        Node<K, V> node = getNode(provider, index, Intent.WRITE);
        
        if (node.isFull()) {
            
            System.out.println("--- NODE: " + node);
            
            Median<K, V> split = node.split(provider);
            
            Entry<K, V> entry = split.getEntry();
            entries.add(index, entry);
            nodes.add(index+1, split.getNodeId());
            
            int cmp = comparator.compare(key, entry.getKey());
            if (0 < cmp) {
                node = getNode(provider, index + 1, Intent.WRITE);
            }
        }
        
        return node.put(provider, key, value);
    }
    
    public Median<K, V> split(NodeProvider<K, V> provider) {
        // I'm left!
        Node<K, V> right = provider.create(leaf);
        
        int size = entries.size();
        int m = size/2;
        assert (m == t-1);
        
        /*Entry<K, V> median = entries.remove(m);
        for (int i = 0; i < (size-m-1); i++) {
            right.add(entries.remove(m));
            
            if (!leaf) {
                right.add(nodes.remove(m+1));
            }
        }
        
        if (!leaf) {
            right.add(nodes.remove(m+1));
        }*/
        
        Entry<K, V> median = entries.remove(t);
        for (int i = 0; i < t-1; i++) {
            right.add(entries.remove(t));
        }
        
        if (!leaf) {
            for (int i = 0; i < t; i++) {
                right.add(nodes.remove(t));
            }
        }
        
        return new Median<K, V>(median, right.getId());
    }
    
    @Override
    public String toString() {
        return nodeId + " @ " + entries + ", " + nodes;
    }
    
    public static class Median<K, V> {
        
        private final Entry<K, V> entry;
        
        private final Id nodeId;

        private Median(Entry<K, V> entry, Id nodeId) {
            this.entry = entry;
            this.nodeId = nodeId;
        }

        public Entry<K, V> getEntry() {
            return entry;
        }

        public Id getNodeId() {
            return nodeId;
        }
        
        @Override
        public String toString() {
            return entry + ", " + nodeId;
        }
    }
    
    public static class Id {
        
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
            } else if (!(o instanceof Id)) {
                return false;
            }
            
            Id other = (Id)o;
            return value == other.value;
        }
        
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
    
    private static class Bucket2<E> {
        
        private final Object[] elements;
        
        private final Id[] nodeIds;
        
        private int size = 0;
        
        public Bucket2(int maxSize) {
            elements = new Object[maxSize];
            nodeIds = new Id[maxSize+1];
        }
        
        public int size() {
            return size;
        }
        
        public boolean isEmpty() {
            return size() == 0;
        }
        
        public boolean isFull() {
            return size() >= elements.length;
        }
        
        public boolean add(E element) {
            add(size, element);
            return true;
        }
        
        public void add(int index, E element) {
            add(index, element, null);
        }
        
        public void add(int index, E element, Id nodeId) {
            if (index != size) {
                System.arraycopy(elements, index, elements, index + 1, size - index);
                
                if (nodeId != null) {
                    System.arraycopy(nodeIds, index+1, nodeIds, index + 2, size - index + 1);
                }
            }
            
            elements[index] = element;
            
            if (nodeId != null) {
                nodeIds[index+1] = nodeId;
            }
            
            ++size;
        }
        
        public E set(int index, E element) {
            E existing = (E)elements[index];
            elements[index] = element;
            return existing;
        }
        
        public E get(int index) {
            return (E)elements[index];
        }
        
        public Id getNodeId(int index) {
            return nodeIds[index];
        }
        
        public Bucket2<E> split() {
            return split((int)Math.ceil(size/2d));
        }
        
        private Bucket2<E> split(int p) {
            Bucket2<E> bucket = new Bucket2<E>(elements.length);
            
            int length = size - p;
            for (int i = 0; i < length; i++) {
                int idx = i + p;
                bucket.elements[i] = elements[idx];
                elements[idx] = null;
                
                bucket.nodeIds[i] = nodeIds[idx];
                nodeIds[idx] = null;
            }
            
            int idx = length + 1;
            bucket.nodeIds[idx] = nodeIds[idx];
            nodeIds[idx] = null;
            
            bucket.size = length;
            size -= length;
            
            return bucket;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            
            sb.append(size)
                .append(toString(elements, 0, size))
                .append(", ")
                .append(toString(nodeIds, 0, size+1));
            
            return sb.append("]").toString();
        }
        
        private static <E> String toString(E[] elements, int offset, int length) {
            StringBuilder sb = new StringBuilder("[");
            
            if (0 < length) {
                for (int i = 0; i < length; i++) {
                    sb.append(elements[offset+i]).append(", ");
                }
                
                sb.setLength(sb.length()-2);
            }
            
            return sb.append("]").toString();
        }
    }
    
    private static class Bucket<E> {
        
        private final Object[] elements;
        
        private int size = 0;
        
        public Bucket(int maxSize) {
            elements = new Object[maxSize];
        }
        
        public int size() {
            return size;
        }
        
        public boolean isEmpty() {
            return size() == 0;
        }
        
        public boolean isFull() {
            return size() >= elements.length;
        }
        
        public void add(E element) {
            add(size, element);
        }
        
        public void add(int index, E element) {
            if (index < 0 || size < index) {
                throw new IndexOutOfBoundsException();
            }
            
            if (isFull()) {
                throw new IllegalStateException();
            }
            
            if (index != size) {
                System.arraycopy(elements, index, elements, index + 1, size - index);
            }
            
            elements[index] = element;
            ++size;
        }
        
        public E set(int index, E element) {
            if (index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException();
            }
            
            E current = (E)elements[index];
            elements[index] = element;
            return current;
        }
        
        public E remove(int index) {
            if (index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException();
            }
            
            E element = (E)elements[index];
            
            int moved = size - index - 1;
            if (moved > 0) {
                System.arraycopy(elements, index+1, elements, index, moved);
            }
            
            elements[--size] = null;
            
            return element;
        }
        
        public void clear() {
            Arrays.fill(null, elements);
            size = 0;
        }
        
        public Bucket<E> divide() {
            return split(size/2);
        }
        
        public Bucket<E> split(int index) {
            Bucket<E> bucket = new Bucket<E>(elements.length);
            
            final int max = size;
            
            while (index < max) {
                E element = (E)elements[index];
                elements[index++] = null;
                
                bucket.add(element);
                --size;
            }
            
            return bucket;
        }
        
        
    }
    
    public static void main(String[] args) {
        Bucket2<String> b = new Bucket2<String>(8);
        
        b.add("P");
        b.add("Q");
        b.add("R");
        b.add("S");
        b.add("T");
        b.add("U");
        b.add("V");
        
        Bucket2<String> o = b.split();
        
        System.out.println(b);
        System.out.println(o);
    }
}