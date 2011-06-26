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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.AbstractBeeTree;
import org.ardverk.btree.Bucket;
import org.ardverk.btree.Node;
import org.ardverk.btree.NodeProvider;
import org.ardverk.btree.Tuple;
import org.ardverk.btree.Node.Median;
import org.ardverk.btree.NodeProvider.Intent;

public class BeeTree<K, V> extends AbstractBeeTree<K, V> implements Closeable {
    
    private static final int T = 128;
    
    private final Object lock = new Object();
    
    private final FileSystemProvider provider = new FileSystemProvider();
    
    private final File directory;
    
    private volatile Node<byte[], byte[]> root = null;
    
    public BeeTree(File directory) {
        this.directory = directory;
        
        File file = new File(directory, "0");
        
        if (!file.exists()) {
            root = provider.allocate(true);
        } else {
            root = provider.load(file);
        }
    }

    @Override
    public void close() throws IOException {
        File file = new File(directory, "0");
        
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            ((StringId)root.getNodeId()).writeTo(out);
        } finally {
            close(out);
        }
        
        provider.store(root);
        
        for (Node<byte[], byte[]> node : provider.nodes.values()) {
            provider.store(node);
        }
    }

    @Override
    public V put(K key, V value) {
        Tuple<K, V> existing = null;
        synchronized (provider) {
            if (root.isOverflow()) {
                Median<byte[], byte[]> median = root.split(provider);
                
                Node<byte[], byte[]> tmp = provider.allocate(false);
                
                tmp.addFirstNodeId(root.getNodeId());
                tmp.addMedian(median);
                
                root = tmp;
            }
            
            existing = root.put(provider, key, value);
        }
        return existing != null ? existing.getValue() : null;
    }

    @Override
    public V remove(K key) {
        Tuple<byte[], byte[]> tuple = null;
        
        synchronized (provider) {
            tuple = root.remove(provider, key);
            
            if (!root.isLeaf() && root.isEmpty()) {
                Node<byte[], byte[]> tmp = root.firstNode(provider, Intent.READ);
                provider.free(root);
                root = tmp;
            }
        }
        
        return tuple != null ? tuple.getValue() : null;
    }

    @Override
    public V get(K key) {
        Tuple<K, V> tuple = root.get(provider, key);
        return tuple != null ? tuple.getValue() : null;
    }
    
    @Override
    public boolean contains(K key) {
        Tuple<K, V> tuple = root.get(provider, key);
        return tuple != null;
    }
    
    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        return root.ceilingTuple(provider, key);
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        return root.firstTuple(provider, Intent.READ);
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        return root.lastTuple(provider, Intent.READ);
    }

    @Override
    public void clear() {
        synchronized (provider) {
            Node<byte[], byte[]> tmp = root;
            root = provider.allocate(true);
            
            provider.free(tmp);
        }
    }

    @Override
    public boolean isEmpty() {
        return root.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        return null;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive) {
        return null;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return null;
    }
    
    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException err) {}
        }
    }
    
    private static void writeBytes(DataOutput out, byte[] data) throws IOException {
        int length = (data != null ? data.length : 0);
        out.writeInt(length);
        
        if (0 < length) {
            out.write(data);
        }
    }
    
    private static byte[] readBytes(DataInput in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }
    
    private class FileSystemProvider implements NodeProvider<byte[], byte[]> {
        
        private final Map<Node.Id, Node<byte[], byte[]>> nodes 
                = new LinkedHashMap<Node.Id, Node<byte[], byte[]>>() {
            
            private static final long serialVersionUID = 9040401273752081840L;
        
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<Node.Id, Node<byte[], byte[]>> eldest) {
                
                Node<byte[], byte[]> node = eldest.getValue();
                
                if (size() >= T) {
                    store(node);
                    return true;
                }
                
                return false;
            }
        };

        @Override
        public Node<byte[], byte[]> allocate(boolean leaf) {
            StringId nodeId = StringId.create();
            return new Node<byte[], byte[]>(nodeId, leaf, T);
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
                close(in);
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
                    
                    int tupleCount = in.readInt();
                    Bucket<Tuple<byte[], byte[]>> tuples 
                        = new Bucket<Tuple<byte[], byte[]>>(tupleCount);
                    
                    for (int i = 0; i < tupleCount; i++) {
                        byte[] key = readBytes(in);
                        byte[] value = readBytes(in);
                        
                        tuples.add(new Tuple<byte[], byte[]>(key, value));
                    }
                    
                    int nodeCount = in.readInt();
                    Bucket<Node.Id> nodes = null;
                    
                    if (0 < nodeCount) {
                        nodes = new Bucket<Node.Id>(nodeCount);
                        for (int i = 0; i < nodeCount; i++) {
                            nodes.add(StringId.valueOf(in));
                        }
                    }
                    
                    assert (nodeCount == 0 || nodeCount-1 == tupleCount);
                    
                    node = new Node<byte[], byte[]>(nodeId, tuples, nodes);
                    
                } catch (IOException err) {
                    throw new IllegalStateException(err);
                } finally {
                    close(in);
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
                    
                    int tupleCount = node.getTupleCount();
                    out.writeInt(tupleCount);
                    for (int i = 0; i < tupleCount; i++) {
                        Tuple<byte[], byte[]> entry = node.getTuple(i);
                        
                        writeBytes(out, entry.getKey());
                        writeBytes(out, entry.getValue());
                    }
                    
                    int nodeCount = node.getNodeCount();
                    out.writeInt(nodeCount);
                    for (int i = 0; i < nodeCount; i++) {
                        Node.Id childId = node.getNodeId(i);
                        
                        ((StringId)childId).writeTo(out);
                    }
                    
                } catch (IOException err) {
                    throw new IllegalStateException(err);
                } finally {
                    close(out);
                }
            }
        }
        
        private void deleteNode(Node<?, ?> node) {
            Node.Id nodeId = node.getNodeId();
            File file = new File(directory, nodeId.toString());
            file.delete();
        }
    }
}
