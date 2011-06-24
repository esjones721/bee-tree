package org.ardverk.btree;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

class Bucket<E> implements List<E>, RandomAccess {

    protected final Object[] elements;
    
    protected final int minSize;
    
    protected int size = 0;
    
    public Bucket(int minSize, int maxSize) {
        this.minSize = minSize;
        this.elements = new Object[maxSize];
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int getMaxSize() {
        return elements.length;
    }
    
    public boolean isOverflow() {
        return size >= elements.length;
    }
    
    public boolean isUnderflow() {
        return size < minSize;
    }
    
    public void addFirst(E element) {
        add(0, element);
    }
    
    public void addLast(E element) {
        add(element);
    }
    
    @Override
    public boolean add(E element) {
        add(size, element);
        return true;
    }
    
    @Override
    public void add(int index, E element) {
        RangeCheck(index);
        
        if (size >= elements.length) {
            throw new ArrayIndexOutOfBoundsException("Max Size");
        }
        
        System.arraycopy(elements, index, elements, index+1, size-index);
        ++size;
        
        elements[index] = element;
    }
    
    @Override
    public E set(int index, E element) {
        RangeCheck(index);
        
        @SuppressWarnings("unchecked")
        E existing = (E)elements[index];
        elements[index] = element;
        return existing;
    }
    
    public E getFirst() {
        return get(0);
    }
    
    public E getLast() {
        return get(size-1);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        RangeCheck(index);
        return (E)elements[index];
    }
    
    public E removeFirst() {
        return remove(0);
    }
    
    public E removeLast() {
        return remove(size-1);
    }
    
    @Override
    public E remove(int index) {
        RangeCheck(index);
        
        @SuppressWarnings("unchecked")
        E element = (E)elements[index];
        
        --size;
        System.arraycopy(elements, index+1, elements, index, size-index);
        elements[size] = null;
        
        return element;
    }
    
    public void addAll(Bucket<? extends E> bucket) {
        addAll(size, bucket);
    }
    
    public void addAll(int index, Bucket<? extends E> bucket) {
        if (index < 0 || elements.length < index+bucket.size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        
        System.arraycopy(elements, index, elements, index + bucket.size, size - index);
        System.arraycopy(bucket.elements, 0, elements, index, bucket.size);
        size += bucket.size;
    }
    
    @Override
    public void clear() {
        Arrays.fill(elements, null);
        size = 0;
    }
    
    @Override
    public Object[] toArray() {
        return toArray(new Object[size]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            Class<?> componentType = a.getClass().getComponentType();
            a = (T[])Array.newInstance(componentType, size);
        }
        
        System.arraycopy(elements, 0, a, 0, size);
        return a;
    }
    
    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    private void RangeCheck(int index) {
        if (index < 0 || size < index) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        if (!isEmpty()) {
            for (int i = 0; i < size; i++) {
                sb.append(elements[i]).append(", ");
            }
            sb.setLength(sb.length()-2);
        }
        
        sb.append("]");
        return sb.toString();
    }
}
