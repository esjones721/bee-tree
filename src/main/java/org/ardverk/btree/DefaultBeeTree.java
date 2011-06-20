package org.ardverk.btree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import org.ardverk.btree.PageProvider2.Intent;

public class DefaultBeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final DefaultPageProvider<K, V> provider 
        = new DefaultPageProvider<K, V>();

    @Override
    public V put(K key, V value) {
        Page2.Id rootId = provider.getRootId();
        
        Page2<K, V> root = provider.get(rootId, Intent.WRITE);
        
        System.out.println("ROOT: " + root + " --- " + key);
        
        if (root.isFull()) {
            
            Node2<K, V> head = root.remove(0);
            Node2<K, V> node = root.split(provider);
            
            root = provider.create(false);
            root.add(new Node2<K, V>(head, rootId));
            root.add(node);
            provider.root = root;
            
            System.out.println("IS: " + root);
            System.out.println("NODE: " + node);
        }
        
        root.put(provider, key, value);
        
        return null;
    }
    
    @Override
    public V get(K key) {
        Page2.Id rootId = provider.getRootId();
        Page2<K, V> root = provider.get(rootId, Intent.READ);
        return root.get(provider, key);
    }

    @Override
    public Comparator<? super K> comparator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V remove(K key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(K key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterator<Entry<K, V>> iterator(K key, boolean inclusive) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String toString() {
        Page2.Id rootId = provider.getRootId();
        Page2<K, V> root = provider.get(rootId, Intent.READ);
        return root.toString(provider);
    }
    
    public static void main(String[] args) {
        DefaultBeeTree<String, String> t 
            = new DefaultBeeTree<String, String>();
        
        //t.put("0", "0");
        //System.out.println(t.get("0"));
        
        for (int i = 0; i < 10; i++) {
            for (int j = 'A'; j <= 'Z'; j++) {
                String key = Character.toString((char)j);
                
                for (int k = 0; k < i; k++) {
                    key += key;
                }
                
                t.put(key, key);
                String foo = t.get(key);
                
                
                if (foo == null) {
                    throw new IllegalStateException(key);
                }
                
                //System.out.println(foo);
            }
        }
        
        System.out.println(t);
        
        System.out.println(t.provider.pages.keySet());
    }
}
