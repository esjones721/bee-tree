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

import java.nio.charset.Charset;

public class StringBinding implements Binding<String> {

    public static final StringBinding BINDING = new StringBinding();
    
    private final Charset encoding;
    
    public StringBinding() {
        this("UTF-8");
    }
    
    public StringBinding(String encoding) {
        this(Charset.forName(encoding));
    }
    
    public StringBinding(Charset encoding) {
        this.encoding = encoding;
    }
    
    @Override
    public byte[] objectToData(String obj) {
        return obj != null ? obj.getBytes(encoding) : null;
    }

    @Override
    public String dataToObject(byte[] data) {
        return data != null ? new String(data, encoding) : null;
    }
}