package org.ardverk.btree.fs;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ardverk.btree.Node;
import org.ardverk.btree.NodeId;
import org.ardverk.btree.NodeProvider;
import org.ardverk.btree.RootNode;

public class FileSystemNodeProvider implements NodeProvider {

    private final Map<NodeId, Node> nodes 
            = new LinkedHashMap<NodeId, Node>() {
        
        private static final long serialVersionUID = 9040401273752081840L;
        
        @Override
        protected boolean removeEldestEntry(
                Map.Entry<NodeId, Node> eldest) {
            
            /*if (size() >= 16) {
                Node node = eldest.getValue();
                return true;
            }*/
            
            return false;
        }
    };
    
    private final File directory;
    
    private final int t;
    
    private final RootNode root;
    
    public FileSystemNodeProvider(File directory, int t) {
        this.directory = directory;
        this.t = t;
        
        Node node = allocate(0);
        root = new RootNode(this, node, 0);
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
        StringId nodeId = StringId.create();
        Node node = new Node(nodeId, height, t);
        nodes.put(nodeId, node);
        return node;
    }

    @Override
    public void free(Node node) {
        nodes.remove(node.getId());
    }

    @Override
    public Node get(NodeId nodeId, Intent intent) {
        return nodes.get(nodeId);
    }
}
