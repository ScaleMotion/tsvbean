package com.scalemotion.tsvbean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EnumType<T extends Enum<T>> implements DataType<T>, ParamsAware, ClassAware{
    private Class cls;
    private Map<String, T> stringIndex;
    private Map<T, String> enumIndex;
    private String keyField;

    @Override
    public void setClass(Class cls) {
        this.cls = cls;
        rebuildIndex();
    }

    private void rebuildIndex() {
        stringIndex = new HashMap<String, T>();
        enumIndex = new HashMap<T, String>();
        if (cls != null) {
            Object[] constants = cls.getEnumConstants();
            for (Object o : constants) {
                String key;
                if (keyField != null) {
                    Field field;
                    try {
                        field = cls.getDeclaredField(keyField);
                    } catch (NoSuchFieldException e) {
                        throw new IllegalStateException("Can'f find field " + keyField + " in enum " + cls.getName(), e);
                    }
                    field.setAccessible(true);
                    try {
                        key = String.valueOf(field.get(o));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Can't get value of field " + cls.getName() + "." + keyField);
                    }
                } else {
                    key = ((Enum)o).name();
                }
                stringIndex.put(key, (T) o);
                enumIndex.put((T) o, key);
            }
        }
    }

    @Override
    public T parse(String str) {
        if (str == null || "null".equals(str)) {
            return null;
        }
        T t = stringIndex.get(str);
        if (t == null) {
            throw new IllegalStateException("Unknown enum key " + str);
        }
        return t;
    }

    @Override
    public String toString(T object) {
        if (object == null) {
            return "null";
        }
        return enumIndex.get(object);
    }

    @Override
    public String describe() {
        return cls.getSimpleName();
    }

    @Override
    public void setParams(String[] args) {
        if (args.length != 0) {
            this.keyField = args[0];
        }
        rebuildIndex();
    }
}
