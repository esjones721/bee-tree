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

package org.ardverk.btree;

import java.util.Comparator;

public interface NodeProvider<K, V> {

    public static enum Intent {
        READ,
        WRITE;
    }
    
    public Node<K, V> getRoot();
    
    public void setRoot(Node<K, V> root);
    
    public Node<K, V> allocate(int height);
    
    public void free(Node<? extends K, ? extends V> node);
    
    public Node<K, V> get(NodeId nodeId, Intent intent);
    
    public Comparator<? super K> comparator();
}
