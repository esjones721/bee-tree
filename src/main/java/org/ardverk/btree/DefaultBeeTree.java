package org.ardverk.btree;

import org.ardverk.btree.PageProvider.Intent;

public class DefaultBeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final PageProvider<K, V> provider 
        = new DefaultPageProvider<K, V>();

    @Override
    public V put(K key, V value) {
        Page.Id rootId = provider.getRootId();
        Page<K, V> root = provider.get(rootId, Intent.WRITE);
        return root.put(provider, key, value);
    }
    
    @Override
    public V get(K key) {
        Page.Id rootId = provider.getRootId();
        Page<K, V> root = provider.get(rootId, Intent.READ);
        return root.get(provider, key);
    }
}
