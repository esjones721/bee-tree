package org.ardverk.btree;

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
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.ardverk.btree.io.DataUtils;
import org.ardverk.btree.io.IoUtils;

public class FileSystemNodeProvider implements NodeProvider, Closeable {

    private final Map<NodeId, Node> nodes 
            = new LinkedHashMap<NodeId, Node>();
    
    private final File directory;
    
    private final int t;
    
    private final int p;
    
    private final RootNode root;
    
    private final ScheduledFuture<?> future;
    
    public FileSystemNodeProvider(String path, int t, int p, long time, TimeUnit unit) {
        this(new File(path), t, p, time, unit);
    }
    
    public FileSystemNodeProvider(File directory, int t, int p, long time, TimeUnit unit) {
        this.directory = directory;
        this.t = t;
        this.p = p;
        
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        root = load(new File(directory, "0"));
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                
            }
        };
        
        future = null;
    }
    
    public File getDirectory() {
        return directory;
    }

    @Override
    public RootNode getRoot() {
        return root;
    }

    @Override
    public Node allocate(int height) {
        return allocate(height, true);
    }
    
    private Node allocate(int height, boolean put) {
        StringId nodeId = StringId.create();
        Node node = new Node(nodeId, height, t);
        Node existing = nodes.put(nodeId, node);
        if (existing != null) {
            throw new IllegalStateException();
        }
        return node;
    }
    
    private boolean isRoot(NodeId nodeId) {
        return nodeId.equals(root.getId());
    }
    
    @Override
    public Node get(NodeId nodeId, Intent intent) {
        if (isRoot(nodeId)) {
            return root.getRoot();
        }
        
        Node node = nodes.get(nodeId);
        if (node == null) {
            node = load(nodeId);
            nodes.put(nodeId, node);
        }
        return node;
    }

    @Override
    public void free(Node node) {
        NodeId nodeId = node.getId();
        nodes.remove(nodeId);
        delete(nodeId);
    }

    @Override
    public void close() {
        
        File file = new File(directory, "0");
        
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            
            out.writeInt(root.size());
            ((StringId)root.getId()).writeTo(out);
            
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(out);
        }
        
        Node rootNode = root.getRoot();
        store(rootNode);
        
        for (Node node : nodes.values()) {
            if (node != rootNode) {
                store(node);
            }
        }
    }
    
    private RootNode load(File file) {
        Node node = null;
        int size = 0;
        
        if (!file.exists()) {
            node = allocate(0, false);
            
        } else {
            StringId nodeId = null;
            DataInputStream in = null;
            try {
                in = new DataInputStream(
                        new BufferedInputStream(
                            new FileInputStream(file)));
                
                size = in.readInt();
                nodeId = StringId.valueOf(in);
                
            } catch (IOException err) {
                throw new IllegalStateException(err);
            } finally {
                IoUtils.close(in);
            }
            
            node = load(nodeId);
            nodes.put(nodeId, node);
        }
        
        return new RootNode(this, node, size);
    }
    
    private Node load(NodeId nodeId) {
        File file = new File(directory, nodeId.toString());
        
        DataInputStream in = null;
        try {
            in = new DataInputStream(
                    new BufferedInputStream(
                        new FileInputStream(file)));
            
            int height = in.readInt();
            
            int tupleCount = in.readInt();
            Bucket<Tuple> tuples = new Bucket<Tuple>(2*t-1);
            
            for (int i = 0; i < tupleCount; i++) {
                byte[] key = DataUtils.readBytes(in);
                byte[] value = DataUtils.readBytes(in);
                
                tuples.add(new Tuple(key, value));
            }
            
            Bucket<NodeId> nodes = null;
            
            if (0 < height) {
                nodes = new Bucket<NodeId>(2*t);

                int nodeCount = in.readInt();
                for (int i = 0; i < nodeCount; i++) {
                    nodes.add(StringId.valueOf(in));
                }
            }
            
            return new Node(nodeId, height, t, tuples, nodes);
            
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(in);
        }
    }
    
    private void store(Node node) {
        NodeId nodeId = node.getId();
        File file = new File(directory, nodeId.toString());
        
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
                Tuple tuple = node.getTuple(i);
                DataUtils.writeBytes(out, tuple.getKey());
                DataUtils.writeBytes(out, tuple.getValue());
            }
            
            if (0 < height) {
                int nodeCount = node.getNodeCount();
                if (tupleCount != nodeCount-1) {
                    throw new IllegalStateException();
                }
                
                out.writeInt(nodeCount);
                for (int i = 0; i < nodeCount; i++) {
                    NodeId childId = node.getNode(i);
                    ((StringId)childId).writeTo(out);
                }
            }
            
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(out);
        }
    }
    
    private void delete(NodeId nodeId) {
        File file = new File(directory, nodeId.toString());
        file.delete();
    }
    
    @Override
    public String toString() {
        return nodes.toString();
    }
    
    private static class StringId implements NodeId {

        private static final Random GENERATOR = new SecureRandom();
        
        private static final char[] ALPHABET = alphabet();
        
        private static final int SIZE = 48;
        
        public static StringId create() {
            char[] nodeId = new char[SIZE];
            
            for (int i = 0; i < nodeId.length; i++) {
                int rnd = GENERATOR.nextInt(ALPHABET.length);
                nodeId[i] = ALPHABET[rnd];
            }
            
            return create(new String(nodeId));
        }
        
        public static StringId create(String nodeId) {
            return new StringId(nodeId);
        }
        
        private final String nodeId;
        
        private StringId(String nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public int hashCode() {
            return nodeId.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof StringId)) {
                return false;
            }
            
            StringId other = (StringId)o;
            return nodeId.equals(other.nodeId);
        }
        
        @Override
        public String toString() {
            return nodeId;
        }
        
        public void writeTo(DataOutput out) throws IOException {
            out.writeUTF(nodeId);
        }
        
        public static StringId valueOf(DataInput in) throws IOException {
            String nodeId = in.readUTF();
            return create(nodeId);
        }
        
        private static char[] alphabet() {
            char[] ch = new char[('Z'-'A' + 1) + ('z'-'a' + 1) + ('9'-'0' + 1)];
            
            int index = 0;
            
            for (int i = 'a'; i <= 'z'; i++) {
                ch[index++] = (char)i;
            }
            
            for (int i = 'A'; i <= 'Z'; i++) {
                ch[index++] = (char)i;
            }
            
            for (int i = '0'; i <= '9'; i++) {
                ch[index++] = (char)i;
            }
            
            return ch;
        }
    }
}
