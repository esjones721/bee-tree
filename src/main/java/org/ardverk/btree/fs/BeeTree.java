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

package org.ardverk.btree.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ardverk.btree.AbstractBeeTree;
import org.ardverk.btree.Bucket;
import org.ardverk.btree.INode.Median;
import org.ardverk.btree.Node;
import org.ardverk.btree.NodeProvider;
import org.ardverk.btree.NodeProvider.Intent;
import org.ardverk.btree.Tuple;
import org.ardverk.btree.io.DataUtils;
import org.ardverk.btree.io.IoUtils;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> implements Closeable {
    
    private static final int T = 128;
    
    private final Object lock = new Object();
    
    private final FileSystemProvider provider;
    
    private final TupleBinding<K, V> binding;
    
    private volatile Node<byte[], byte[]> root = null;
    
    public BeeTree(File directory, TupleBinding<K, V> binding) {
        this(directory, binding, T);
    }
    
    public BeeTree(File directory, TupleBinding<K, V> binding, int t) {
        this.provider = new FileSystemProvider(directory, t);
        
        this.binding = binding;
        
        File file = new File(directory, "0");
        
        if (!file.exists()) {
            root = provider.allocate(0);
        } else {
            root = provider.load(file);
        }
    }
    
    private Map.Entry<K, V> tupleToEntry(Tuple<byte[], byte[]> tuple) {
        return tuple != null ? binding.tupleToEntry(tuple) : null;
    }
    
    private K tupleToKey(Tuple<byte[], ?> tuple) {
        return tuple != null ? binding.entryToKey(tuple.getKey()) : null;
    }
    
    private V tupleToValue(Tuple<?, byte[]> tuple) {
        return tuple != null ? binding.entryToValue(tuple.getValue()) : null;
    }

    @Override
    public void close() throws IOException {
        File file = new File(provider.directory, "0");
        
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            ((StringId)root.getNodeId()).writeTo(out);
        } finally {
            IoUtils.close(out);
        }
        
        provider.store(root);
        
        for (Node<byte[], byte[]> node : provider.nodes.values()) {
            provider.store(node);
        }
    }

    @Override
    public V put(K key, V value) {
        Tuple<byte[], byte[]> existing = null;
        synchronized (provider) {
            if (root.isOverflow()) {
                Median<byte[], byte[]> median = root.split(provider);
                
                int height = root.getHeight() + 1;
                Node<byte[], byte[]> tmp = provider.allocate(height);
                
                tmp.addFirstNodeId(root.getNodeId());
                tmp.addMedian(median);
                
                root = tmp;
            }
            
            existing = root.put(provider, 
                    binding.objectToKey(key), 
                    binding.objectToValue(value));
        }
        return tupleToValue(existing);
    }

    @Override
    public V remove(K key) {
        Tuple<byte[], byte[]> tuple = null;
        
        synchronized (provider) {
            tuple = root.remove(provider, 
                    binding.objectToKey(key));
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node<byte[], byte[]> tmp = root.firstNode(
                        provider, Intent.READ);
                provider.free(root);
                root = tmp;
            }
        }
        
        return tupleToValue(tuple);
    }

    @Override
    public V get(K key) {
        Tuple<byte[], byte[]> tuple = root.get(provider, 
                binding.objectToKey(key));
        return tupleToValue(tuple);
    }
    
    @Override
    public boolean contains(K key) {
        Tuple<byte[], byte[]> tuple = root.get(provider, 
                binding.objectToKey(key));
        return tuple != null;
    }
    
    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        Tuple<byte[], byte[]> tuple 
            = root.ceilingTuple(provider, 
                binding.objectToKey(key));
        return tupleToEntry(tuple);
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        Tuple<byte[], byte[]> tuple = root.firstTuple(provider, Intent.READ);
        return tupleToEntry(tuple);
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        Tuple<byte[], byte[]> tuple = root.lastTuple(provider, Intent.READ);
        return tupleToEntry(tuple);
    }

    @Override
    public void clear() {
        synchronized (provider) {
            Node<byte[], byte[]> tmp = root;
            root = provider.allocate(0);
            
            provider.free(tmp);
        }
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive) {
        Iterator<Tuple<byte[], byte[]>> it = root.iterator(
                provider, binding.objectToKey(key), inclusive);
        return new EntryIterator(it);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        Iterator<Tuple<byte[], byte[]>> it = root.iterator(provider);
        return new EntryIterator(it);
    }
    
    private class FileSystemProvider implements NodeProvider<byte[], byte[]> {
        
        private final Map<Node.Id, Node<byte[], byte[]>> nodes 
                = new LinkedHashMap<Node.Id, Node<byte[], byte[]>>() {
            
            private static final long serialVersionUID = 9040401273752081840L;
        
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<Node.Id, Node<byte[], byte[]>> eldest) {
                
                Node<byte[], byte[]> node = eldest.getValue();
                
                if (size() >= 16) {
                    store(node);
                    return true;
                }
                
                return false;
            }
        };

        private final File directory;
        
        private final int t;
        
        public FileSystemProvider(File directory, int t) {
            this.directory = directory;
            this.t = t;
        }
        
        @Override
        public Node<byte[], byte[]> allocate(int height) {
            StringId nodeId = StringId.create();
            
            Node<byte[], byte[]> node 
                = new Node<byte[], byte[]>(
                    nodeId, height, t);
            
            nodes.put(nodeId, node);
            
            return node;
        }

        @Override
        public void free(Node<? extends byte[], ? extends byte[]> node) {
            nodes.remove(node.getNodeId());
            deleteNode(node);
        }

        @Override
        public Node<byte[], byte[]> get(Node.Id nodeId, Intent intent) {
            Node<byte[], byte[]> node = nodes.get(nodeId);
            if (node == null) {
                node = load(nodeId);
                nodes.put(nodeId, node);
            }
            return node;
        }

        @Override
        public Comparator<? super byte[]> comparator() {
            return ByteArrayComparator.COMPARATOR;
        }
        
        private Node<byte[], byte[]> load(File file) {
            StringId nodeId = null;
            DataInputStream in = null;
            try {
                in = new DataInputStream(
                        new BufferedInputStream(
                            new FileInputStream(file)));
                
                nodeId = StringId.valueOf(in);
            } catch (IOException err) {
                throw new IllegalStateException(err);
            } finally {
                IoUtils.close(in);
            }
            
            return load(nodeId);
        }
        
        private Node<byte[], byte[]> load(Node.Id nodeId) {
            File file = new File(directory, nodeId.toString());
            
            Node<byte[], byte[]> node = null;
            
            synchronized (lock) {
                DataInputStream in = null;
                try {
                    in = new DataInputStream(
                            new BufferedInputStream(
                                new FileInputStream(file)));
                    
                    int height = in.readInt();
                    
                    int tupleCount = in.readInt();
                    Bucket<Tuple<byte[], byte[]>> tuples 
                        = new Bucket<Tuple<byte[], byte[]>>(2*t-1);
                    
                    for (int i = 0; i < tupleCount; i++) {
                        byte[] key = DataUtils.readBytes(in);
                        byte[] value = DataUtils.readBytes(in);
                        
                        tuples.add(new Tuple<byte[], byte[]>(key, value));
                    }
                    
                    Bucket<Node.Id> nodes = null;
                    
                    if (0 < height) {
                        nodes = new Bucket<Node.Id>(2*t);

                        int nodeCount = in.readInt();
                        for (int i = 0; i < nodeCount; i++) {
                            nodes.add(StringId.valueOf(in));
                        }
                    }
                    
                    node = new Node<byte[], byte[]>(nodeId, 
                            height, t, tuples, nodes);
                    
                } catch (IOException err) {
                    throw new IllegalStateException(err);
                } finally {
                    IoUtils.close(in);
                }
            }
            
            return node;
        }
        
        private void store(Node<byte[], byte[]> node) {
            Node.Id nodeId = node.getNodeId();
            File file = new File(directory, nodeId.toString());
            
            synchronized (lock) {
                DataOutputStream out = null;
                try {
                    out = new DataOutputStream(
                            new BufferedOutputStream(
                                new FileOutputStream(file)));
                    
                    int height = node.getHeight();
                    out.writeInt(height);
                    
                    int tupleCount = node.getTupleCount();
                    out.writeInt(tupleCount);
                    for (int i = 0; i < tupleCount; i++) {
                        Tuple<byte[], byte[]> tuple = node.getTuple(i);
                        DataUtils.writeBytes(out, tuple.getKey());
                        DataUtils.writeBytes(out, tuple.getValue());
                    }
                    
                    if (0 < height) {
                        int nodeCount = node.getNodeCount();
                        out.writeInt(nodeCount);
                        for (int i = 0; i < nodeCount; i++) {
                            Node.Id childId = node.getNodeId(i);
                            ((StringId)childId).writeTo(out);
                        }
                    }
                    
                } catch (IOException err) {
                    throw new IllegalStateException(err);
                } finally {
                    IoUtils.close(out);
                }
            }
        }
        
        private void deleteNode(Node<?, ?> node) {
            Node.Id nodeId = node.getNodeId();
            File file = new File(directory, nodeId.toString());
            file.delete();
        }
    }
    
    private class EntryIterator implements Iterator<Map.Entry<K, V>> {
        
        private final Iterator<Tuple<byte[], byte[]>> it;

        public EntryIterator(Iterator<Tuple<byte[], byte[]>> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return tupleToEntry(it.next());
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
    
    public static void main(String[] args) throws Exception {
        File directory = new File("tree");
        directory.mkdirs();
        TupleBinding<String, String> binding 
            = new TupleBinding<String, String>(
                StringEntryBinding.BINDING, 
                StringEntryBinding.BINDING);
        
        BeeTree<String, String> tree = new BeeTree<String, String>(
                directory, binding);
        
        foo(tree);
        
        tree.close();
    }
    
    private static void foo(BeeTree<String, String> tree) {
        long startTime = System.currentTimeMillis();
        int count = 0;
        for (int i = 'A'; i <= 'Z'; i++) {
            for (int j = 0; j < 1000; j++) {
                String key = Character.toString((char)i);
                for (int k = 0; k < j; k++) {
                    key += Character.toString((char)i);
                }
                
                tree.put(key, key);
                ++count;
                
                String found = tree.get(key);
                if (found == null || !found.equals(key)) {
                    throw new IllegalStateException("key=" + key + ", found=" + found);
                }
            }
        }
        
        long time = System.currentTimeMillis() - startTime;
        System.out.println("Done: " + count + ", " + time);
    }
}
