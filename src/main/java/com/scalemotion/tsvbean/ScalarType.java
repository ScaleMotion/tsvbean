package com.scalemotion.tsvbean;

import com.google.common.base.Defaults;
import com.google.common.primitives.Primitives;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class ScalarType<T> implements ClassAware, DataType<T>, BinaryType<T> {
    private static Map<Class, String> binaryMethodPrefix = new HashMap<Class, String>();
    static {
        binaryMethodPrefix.put(Long.class, "Long");
        binaryMethodPrefix.put(Integer.class, "Int");
        binaryMethodPrefix.put(Short.class, "Short");
        binaryMethodPrefix.put(Byte.class, "Byte");
        binaryMethodPrefix.put(Double.class, "Double");
        binaryMethodPrefix.put(Boolean.class, "Boolean");
        binaryMethodPrefix.put(Character.class, "Char");
        binaryMethodPrefix.put(Float.class, "Float");
    }
    private Class<T> scalarClass;
    private Method method;
    private T nullValue = null;
    private Method binaryReadMethod;
    private Method binaryWriteMethod;

    public static <T> ScalarType<T> forClass(Class<T> scalarClass) {
        ScalarType<T> type = new ScalarType<T>();
        type.setClass(scalarClass);
        return type;
    }

    public ScalarType() {
    }

    public T parse(String str) {
        if (method == null) {
            return (T) str;
        }
        if (str == null || str.equals("null")) {
            return nullValue;
        }
        try {
            return (T) method.invoke(null, str);
        } catch (Exception e) {
            throw new IllegalStateException("Can't parse " + str + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString(T object) {
        return object == null ? "null" : object.toString();
    }

    @Override
    public String describe() {
        return scalarClass.getSimpleName();
    }


    @Override
    public void setClass(Class cls) {
        if (cls.isPrimitive()) {
            nullValue = (T) Defaults.defaultValue(cls);
            String name;
            if (cls == int.class) {
                name = Integer.class.getName();
            } else if (cls == char.class) {
                name = Character.class.getName();
            } else {
                name = "java.lang." + Character.toUpperCase(cls.getName().charAt(0)) + cls.getName().substring(1);
            }
            try {
                cls = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        scalarClass = cls;
        if (cls != String.class) {
            try {
                method = scalarClass.getMethod("valueOf", new Class[]{String.class});
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Class " + scalarClass.getName() + " is not scalar! There's no valueOf(String) method");
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Method " + scalarClass.getName() + ".valueOf() is not static");
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("Method " + scalarClass.getName() + ".valueOf() is not public");
            }
            try {
                binaryWriteMethod = DataOutput.class.getMethod("write" + binaryMethodPrefix.get(scalarClass), Primitives.unwrap(scalarClass));
                binaryReadMethod = DataInput.class.getMethod("read" + binaryMethodPrefix.get(scalarClass));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        } else {
            try {
                binaryWriteMethod = DataOutput.class.getMethod("writeUTF", String.class);
                binaryReadMethod = DataInput.class.getMethod("readUTF");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public Class<T> getScalarClass() {
        return scalarClass;
    }

    @Override
    public T read(DataInput in) {
        try {
            return (T) binaryReadMethod.invoke(in);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void write(T obj, DataOutput out) {
        try {
            binaryWriteMethod.invoke(out, obj);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
