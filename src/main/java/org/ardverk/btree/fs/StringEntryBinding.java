package org.ardverk.btree.fs;

import java.nio.charset.Charset;

public class StringEntryBinding implements EntryBinding<String> {

    private static final String UTF8 = "UTF8";
    
    public static final StringEntryBinding BINDING = new StringEntryBinding();
    
    private final Charset encoding;
    
    public StringEntryBinding() {
        this(UTF8);
    }
    
    public StringEntryBinding(String encoding) {
        this(Charset.forName(encoding));
    }
    
    public StringEntryBinding(Charset encoding) {
        this.encoding = encoding;
    }
    
    @Override
    public String entryToObject(byte[] entry) {
        return new String(entry, encoding);
    }

    @Override
    public byte[] objectToEntry(String element) {
        return element.getBytes(encoding);
    }
}
