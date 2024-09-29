package ru.abstractmenus.util;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayListIterator<T> implements Iterator<T> {

    private final ArrayList<T> list;
    private int pointer = -1;

    public ArrayListIterator(ArrayList<T> list) {
        this.list = list;
    }

    public void skip(int count) {
        pointer += count;
        pointer = Math.min(pointer, list.size() - 1);
    }

    @Override
    public boolean hasNext() {
        return pointer < list.size() - 1;
    }

    @Override
    public T next() {
        skip(1);
        return list.get(pointer);
    }
}
