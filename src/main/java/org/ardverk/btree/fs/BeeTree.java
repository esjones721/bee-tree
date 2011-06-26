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

public class BeeTree<K, V> extends AbstractBeeTree<K, V> implements Closeable {
    
    private static final int T = 128;
    
    private final Object lock = new Object();
    
    private final Map<Node.Id, Node<byte[], byte[]>> nodes 
            = new LinkedHashMap<Node.Id, Node<byte[], byte[]>>() {
        
        private static final long serialVersionUID = 9040401273752081840L;

        @Override
        protected boolean removeEldestEntry(
                Map.Entry<Node.Id, Node<byte[], byte[]>> eldest) {
            if (size() >= T) {
                store(eldest.getValue());
                return true;
            }
            return false;
        }
    };
    
    private final NodeProvider<byte[], byte[]> provider 
            = new NodeProvider<byte[], byte[]>() {

        @Override
        public Node<byte[], byte[]> allocate(boolean leaf) {
            return null;
        }

        @Override
        public void free(Node<? extends byte[], ? extends byte[]> node) {
            nodes.remove(node.getNodeId());
            deleteNode(node);
        }

        @Override
        public Node<byte[], byte[]> get(Node.Id nodeId, Intent intent) {
            return null;
        }

        @Override
        public Comparator<? super byte[]> comparator() {
            return ByteArrayComparator.COMPARATOR;
        }
    };
    
    private final File directory;
    
    public BeeTree(File directory) {
        this.directory = directory;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V get(K key) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean contains(K key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator(K key, boolean inclusive) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private Node<byte[], byte[]> load(Node.Id nodeId) {
        File file = new File(directory, nodeId.toString());
        
        synchronized (lock) {
            DataInputStream in = null;
            try {
                in = new DataInputStream(
                        new BufferedInputStream(
                            new FileInputStream(file)));
                
                //StringId.valueOf(in);
                
                int entryCount = in.readInt();
                Bucket<Tuple<byte[], byte[]>> entries 
                    = new Bucket<Tuple<byte[], byte[]>>(entryCount);
                
                for (int i = 0; i < entryCount; i++) {
                    byte[] key = readBytes(in);
                    byte[] value = readBytes(in);
                    
                    entries.add(new Tuple<byte[], byte[]>(key, value));
                }
                
                int nodeCount = in.readInt();
                Bucket<Node.Id> nodes = null;
                
                if (0 < nodeCount) {
                    nodes = new Bucket<Node.Id>(nodeCount);
                    for (int i = 0; i < nodeCount; i++) {
                        nodes.add(StringId.valueOf(in));
                    }
                }
                
                assert (nodeCount == 0 || nodeCount-1 == entryCount);
                
                return new Node<byte[], byte[]>(nodeId, entries, nodes);
                
            } catch (IOException err) {
                throw new IllegalStateException(err);
            } finally {
                close(in);
            }
        }
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
                
                //((StringId)nodeId).writeTo(out);
                
                int entryCount = node.getTupleCount();
                out.writeInt(entryCount);
                for (int i = 0; i < entryCount; i++) {
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
}
