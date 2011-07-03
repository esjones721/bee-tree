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

import junit.framework.TestCase;

import org.junit.Test;

public class BucketTest {

    @Test
    public void addAll() {
        Bucket<String> a = new Bucket<String>(6);
        a.add("a");
        a.add("b");
        a.add("c");
        
        TestCase.assertEquals(3, a.size());
        
        Bucket<String> b = new Bucket<String>(3);
        b.add("1");
        b.add("2");
        b.add("3");
        
        TestCase.assertEquals(3, b.size());
        
        a.addAll(b);
        
        TestCase.assertEquals(6, a.size());
    }
}