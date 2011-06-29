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
