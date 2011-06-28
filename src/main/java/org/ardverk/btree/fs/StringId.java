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

package org.ardverk.btree.fs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import org.ardverk.btree.NodeId;

public class StringId implements NodeId, CharSequence {

    private static final Random GENERATOR = new SecureRandom();
    
    private static final char[] ALPHABET = alphabet();
    
    private static final int SIZE = 48;
    
    public static StringId create() {
        char[] nodeId = new char[SIZE];
        
        for (int i = 0; i < nodeId.length; i++) {
            int rnd = GENERATOR.nextInt(ALPHABET.length);
            nodeId[i] = ALPHABET[rnd];
        }
        
        return create(new String(nodeId));
    }
    
    public static StringId create(String nodeId) {
        return new StringId(nodeId);
    }
    
    private final String nodeId;
    
    private StringId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    @Override
    public int length() {
        return nodeId.length();
    }

    @Override
    public char charAt(int index) {
        return nodeId.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return nodeId.subSequence(start, end);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof StringId)) {
            return false;
        }
        
        StringId other = (StringId)o;
        return nodeId.equals(other.nodeId);
    }
    
    @Override
    public String toString() {
        return nodeId;
    }
    
    public void writeTo(DataOutput out) throws IOException {
        out.writeUTF(nodeId);
    }
    
    public static StringId valueOf(DataInput in) throws IOException {
        String nodeId = in.readUTF();
        return create(nodeId);
    }
    
    private static char[] alphabet() {
        char[] ch = new char[('Z'-'A' + 1) + ('z'-'a' + 1) + ('9'-'0' + 1)];
        
        int index = 0;
        
        for (int i = 'a'; i <= 'z'; i++) {
            ch[index++] = (char)i;
        }
        
        for (int i = 'A'; i <= 'Z'; i++) {
            ch[index++] = (char)i;
        }
        
        for (int i = '0'; i <= '9'; i++) {
            ch[index++] = (char)i;
        }
        
        return ch;
    }
}
