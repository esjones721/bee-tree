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

public class DefaultTupleBinding<K, V> implements TupleBinding<K, V> {
    
    public static <T> TupleBinding<T, T> create(Binding<T> binding) {
        return new DefaultTupleBinding<T, T>(binding, binding);
    }
    
    public static <K, V> TupleBinding<K, V> create(Binding<K> kb, Binding<V> vb) {
        return new DefaultTupleBinding<K, V>(kb, vb);
    }
    
    private final Binding<K> kb;
    
    private final Binding<V> vb;
    
    public DefaultTupleBinding(Binding<K> kb, Binding<V> vb) {
        this.kb = kb;
        this.vb = vb;
    }

    @Override
    public byte[] objectToKey(K key) {
        return kb.objectToData(key);
    }

    @Override
    public K keyToObject(byte[] key) {
        return kb.dataToObject(key);
    }

    @Override
    public byte[] objectToValue(V value) {
        return vb.objectToData(value);
    }

    @Override
    public V valueToObject(byte[] value) {
        return vb.dataToObject(value);
    }
}