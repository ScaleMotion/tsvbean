package com.scalemotion.tsvbean;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utilities for working with binary data ({@link BinaryRecordMapper})
 */
public class BinaryHelper {
    /**
     * Prevent instantion
     */
    private BinaryHelper() {}

    /**
     * Converts object to byte array with given binary type (aka serializer)
     * @param object object
     * @param type type (aka serializer)
     * @param <T> type of object
     * @return byte array
     */
    public static <T> byte[] toBytes(T object, BinaryType<T> type) {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        try {
            type.write(object, new DataOutputStream(buff));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return buff.toByteArray();
    }

    /**
     * Restores object from byte array
     * @param b byte array
     * @param type type (aka serializer)
     * @param offset offset in byte array
     * @param <T> type of object
     * @return deserialized object
     */
    public static <T> T fromBytes(byte[] b, BinaryType<T> type, int offset)  {
        try {
            return type.read(new DataInputStream(new ByteArrayInputStream(b, offset, b.length)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Converts long to byte array
     * @param l long value
     * @param array array where put result
     * @param offset offset in target array
     */
    public static void toBytes(long l, byte[] array, int offset) {
        ByteBuffer buff = ByteBuffer.allocate(8);
        buff.putLong(0, l);
        byte[] bytes = buff.array();
        System.arraycopy(bytes, 0, array, offset, bytes.length);
    }

    /**
     * Converts int to byte array
     * @param l int value
     * @param array array where put result
     * @param offset offset in target array
     */
    public static void toBytes(int l, byte[] array, int offset) {
        ByteBuffer buff = ByteBuffer.allocate(4);
        buff.putInt(0, l);
        byte[] bytes = buff.array();
        System.arraycopy(bytes, 0, array, offset, bytes.length);
    }

    /**
     * Converts int to byte array
     * @param l int value
     * @param array array where put result
     */
    public static void toBytes(int l, byte[] array) {
        toBytes(l, array, 0);
    }


    /**
     * Convert long value to byte array (array will be instantiated)
     * @param l long value
     * @return new byte array
     */
    public static byte[] toBytes(long l) {
        byte b[] = new byte[8];
        toBytes(l, b, 0);
        return b;
    }

    /**
     * Restores long value from byte array
     * @param array array
     * @param offset offset in array
     * @return long value
     */
    public static long longFromBytes(byte[] array, int offset) {
        ByteBuffer buff = ByteBuffer.allocate(8);
        System.arraycopy(array, offset, buff.array(), 0, 8);
        return buff.getLong(0);
    }

    /**
     * Restores int value from byte array
     * @param array array
     * @param offset offset in array
     * @return int value
     */
    public static int intFromBytes(byte[] array, int offset) {
        ByteBuffer buff = ByteBuffer.allocate(4);
        System.arraycopy(array, offset, buff.array(), 0, 4);
        return buff.getInt();
    }

    /**
     * Restores int value from byte array
     * @param array array
     * @return int value
     */
    public static int intFromBytes(byte[] array) {
        return intFromBytes(array, 0);
    }

    /**
     * Restores long from byte array (with 0 offset)
     * @param b1 byte array
     * @return long value
     */
    public static long longFromBytes(byte[] b1) {
        return longFromBytes(b1, 0);
    }

    /**
     * Converts UUID to byte array
     * @param uuid uuid
     * @param array target array
     * @param offset offset in array
     */
    public static void toBytes(UUID uuid, byte[] array, int offset) {
        toBytes(uuid.getLeastSignificantBits(), array, offset);
        toBytes(uuid.getMostSignificantBits(), array, offset + 8);
    }

    /**
     * Creates a byte array from uuid
     * @param uuid uuid
     * @return newly created byte array
     */
    public static byte[] toBytes(UUID uuid) {
        byte[] result = new byte[16];
        toBytes(uuid, result, 0);
        return result;
    }

    /**
     * Restores UUID from byte array
     * @param array array
     * @param offset offset in array
     * @return restored uuid
     */
    public static UUID uuidFromBytes(byte[] array, int offset) {
        long least = longFromBytes(array, offset);
        long most = longFromBytes(array, offset + 8);
        return new UUID(most, least);
    }

    /**
     * Restores UUID from byte array (with 0 offset)
     * @param b1 byte array
     * @return uuid
     */
    public static UUID uuidFromBytes(byte[] b1) {
        return uuidFromBytes(b1, 0);
    }

    /**
     * Stores long in byte array (with 0 offset)
     * @param value long value
     * @param bb byte array
     */
    public static void toBytes(long value, byte[] bb) {
        toBytes(value, bb, 0);
    }
}
