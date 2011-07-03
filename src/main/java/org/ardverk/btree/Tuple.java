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

import java.util.Map;
import java.util.Map.Entry;

/**
 * An immutable Key-Value pair. It implements the {@link Entry} interface
 * for convenience/compatibility purposes. 
 */
public class Tuple implements Map.Entry<byte[], byte[]> {

    private final byte[] key;
    
    private final byte[] value;
    
    public Tuple(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public byte[] getKey() {
        return key;
    }

    @Override
    public byte[] getValue() {
        return value;
    }
    
    @Override
    public byte[] setValue(byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        //return Arrays.toString(key) + "=" + Arrays.toString(value);
        return new String(key) + "=" + new String(value);
    }
}