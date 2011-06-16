package org.ardverk.btree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class Node<K, V> {

    private final List<Entry<K, V>> entries;
    
    public Node(int pageSize) {
        entries = new ArrayList<Entry<K, V>>(pageSize);
    }
    
    public int size() {
        return entries.size();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        
    }
    
    public static <K, V> Node<K, V> valueOf(InputStream in) throws IOException {
        return null;
    }
    
    public static class Id {

    }
}
