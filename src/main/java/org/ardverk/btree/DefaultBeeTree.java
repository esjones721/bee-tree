package org.ardverk.btree;

import org.ardverk.btree.NodeProvider.Intent;

public class DefaultBeeTree<K, V> extends AbstractBeeTree<K, V> {

    private final NodeProvider<K, V> provider = null;

    @Override
    public V put(K key, V value) {
        Node.Id rootId = provider.getRootId();
        Node<K, V> root = provider.get(rootId, Intent.WRITE);
        return root.put(key, value);
    }
}
