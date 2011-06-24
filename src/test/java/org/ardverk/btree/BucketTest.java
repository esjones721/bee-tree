package org.ardverk.btree;

import org.ardverk.btree.Bucket;
import org.junit.Test;

public class BucketTest {

    @Test
    public void main() {
        Bucket<String> a = new Bucket<String>(0, 6);
        a.add("a");
        a.add("b");
        a.add("c");
        
        System.out.println(a);
        
        Bucket<String> b = new Bucket<String>(0, 3);
        b.add("1");
        b.add("2");
        b.add("3");
        
        System.out.println(b);
        
        a.addAll(b);
        
        System.out.println(a);
        
        System.out.println(a.remove(3));
        System.out.println(a);
    }
}
