/*
 * Copyright 2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.btree;

abstract class AbstractNode {

    protected final NodeId nodeId;
    
    protected final int height;
    
    protected final int t;
    
    public AbstractNode(NodeId nodeId, int height, int t) {
        this.nodeId = nodeId;
        this.height = height;
        this.t = t;
    }
    
    public NodeId getId() {
        return nodeId;
    }

    public int getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return getTupleCount() == 0;
    }

    public boolean isOverflow() {
        return getTupleCount() >= 2*t-1;
    }

    public boolean isUnderflow() {
        return getTupleCount() < t;
    }

    public boolean isLeaf() {
        return getHeight() == 0;
    }
    
    public abstract int getTupleCount();
    
    public abstract int getNodeCount();
}