package org.ardverk.btree;

import java.util.Comparator;

public abstract class BeeTreeNodeProvider extends AbstractNodeProvider<byte[], byte[]> {

    public BeeTreeNodeProvider() {
        super();
    }

    public BeeTreeNodeProvider(Comparator<? super byte[]> comparator) {
        super(comparator);
    }
}
