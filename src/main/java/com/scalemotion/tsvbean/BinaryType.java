package com.scalemotion.tsvbean;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface BinaryType<T> {
    public T read(DataInput in) throws IOException;
    public void write(T obj, DataOutput out) throws IOException;
}
