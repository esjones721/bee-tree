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

package org.ardverk.btree.memory;

import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.btree.Node;

class IntegerId implements Node.Id {
    
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
        } else if (!(o instanceof IntegerId)) {
            return false;
        }
        
        IntegerId other = (IntegerId)o;
        return value == other.value;
    }
    
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}