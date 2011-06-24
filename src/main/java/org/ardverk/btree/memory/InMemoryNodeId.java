package org.ardverk.btree.memory;

import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree.NodeId;

class InMemoryNodeId implements NodeId {
    
    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private final int value = COUNTER.incrementAndGet();
    
    @Override
    public int hashCode() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof InMemoryNodeId)) {
            return false;
        }
        
        InMemoryNodeId other = (InMemoryNodeId)o;
        return value == other.value;
    }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}