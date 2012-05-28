package com.scalemotion.tsvbean;

public interface DataType<T> {
    public T parse(String str);
    public String toString(T object);
    public String describe();
}
