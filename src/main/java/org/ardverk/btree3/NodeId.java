package org.ardverk.btree3;

import java.util.concurrent.atomic.AtomicInteger;

public class NodeId {
    
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
        } else if (!(o instanceof NodeId)) {
            return false;
        }
        
        NodeId other = (NodeId)o;
        return value == other.value;
    }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}