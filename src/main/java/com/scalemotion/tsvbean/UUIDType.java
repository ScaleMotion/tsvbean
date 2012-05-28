package com.scalemotion.tsvbean;

import com.google.common.base.Strings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class UUIDType implements DataType<UUID>, BinaryType<UUID>{
    @Override
    public UUID parse(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        }
        return UUID.fromString(str);
    }

    @Override
    public String toString(UUID object) {
        return object == null ? "" : object.toString();
    }

    @Override
    public String describe() {
        return "UUID";
    }

    @Override
    public UUID read(DataInput in) throws IOException {
        byte[] uuidBytes = new byte[16];
        in.readFully(uuidBytes);
        return BinaryHelper.uuidFromBytes(uuidBytes);
    }

    @Override
    public void write(UUID obj, DataOutput out) throws IOException {
        byte[] uuidBytes = BinaryHelper.toBytes(obj);
        out.write(uuidBytes);
    }
}
