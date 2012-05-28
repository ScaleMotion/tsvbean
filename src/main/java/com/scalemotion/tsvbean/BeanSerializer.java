package com.scalemotion.tsvbean;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.*;

public class BeanSerializer<T> implements DataType<T>, ClassAware<T>, ParamsAware  {
    public static final String DEFAULT_SEPARATOR = "\t";
    private Class<T> cls;
    private String separator = DEFAULT_SEPARATOR;
    private LinkedHashMap<String, FieldInfo> fieldsDictionary = new LinkedHashMap<String, FieldInfo>();
    private List<FieldInfo> fieldList = new ArrayList<FieldInfo>();

    public BeanSerializer(Class<T> cls, String separator, String[] header) {
        this.cls = cls;
        this.separator = separator;
        buildFieldDictionary();
        setHeader(header);
    }

    public BeanSerializer(Class<T> cls, String separator, String header) {
        this(cls, separator, Iterables.toArray(Splitter.on(separator).split(header), String.class));
    }

    public BeanSerializer(Class<T> cls, String separator) {
        this(cls, separator, new String[]{});
    }

    public BeanSerializer(Class<T> cls) {
        this(cls, DEFAULT_SEPARATOR);
    }

    public BeanSerializer() {
    }

    public void setHeader(String[] header) {
        if (header == null || header.length == 0) {
            return;
        }
        if (fieldsDictionary == null) {
            throw new IllegalStateException("buildFieldDictionary() should be called before setHeader()");
        }
        fieldList = new ArrayList<FieldInfo>();
        for (String h : header) {
            FieldInfo fieldInfo = fieldsDictionary.get(h);
            if (fieldInfo != null) {
                fieldList.add(fieldInfo);
            }
        }

    }

    private LinkedHashMap<String, FieldInfo> listFields(Class cls, String namePrefix, List<java.lang.reflect.Field> chain) {
        LinkedHashMap<String, FieldInfo> res = new LinkedHashMap<String, FieldInfo>();
        for (java.lang.reflect.Field f : getAllFields(cls)) {
            ArrayList<java.lang.reflect.Field> newChain = new ArrayList<java.lang.reflect.Field>(chain);
            newChain.add(f);
            Field annotation = f.getAnnotation(Field.class);
            if (annotation != null) {
                String name = namePrefix + (annotation.name().isEmpty() ? f.getName() : annotation.name());
                DataType dataType;
                Class<? extends DataType> type = annotation.type();
                if (type == DataType.class) {
                    //no type specified. Guessing it
                    if (Date.class.isAssignableFrom(f.getType())) {
                        type = DateTimeType.class;
                    } else if (Enum.class.isAssignableFrom(f.getType())) {
                        type = EnumType.class;
                    } else {
                        type = ScalarType.class;
                    }
                }
                try {
                    dataType = newInstance(type);
                } catch (Exception e) {
                    throw new IllegalStateException("Can't create instance of format class " + type.getClass() + ": " + e.getMessage(), e);
                }
                if (dataType instanceof ClassAware) {
                    ((ClassAware) dataType).setClass(f.getType());
                }
                if (dataType instanceof ParamsAware && annotation.args().length != 0) {
                    ((ParamsAware) dataType).setParams(annotation.args());
                }
                if (res.put(name, new FieldInfo(name, dataType, newChain)) != null) {
                    throw new IllegalStateException("Can't register field with name '" + name + "'. Field with same name was already registered");
                }
            }
            EmbeddedDataField em = f.getAnnotation(EmbeddedDataField.class);
            f.setAccessible(true);
            if (em != null) {
                String prefix = em.fieldPrefix();
                if (prefix.equals(EmbeddedDataField.INHERIT_PREFIX)) {
                    prefix = f.getName() + "_";
                }
                LinkedHashMap<String, FieldInfo> embedded = listFields(f.getType(), namePrefix + prefix, newChain);
                for (Map.Entry<String, FieldInfo> e : embedded.entrySet()) {
                    if (res.put(e.getKey(), e.getValue()) != null) {
                        throw new IllegalStateException("Can't register field with name '" + e.getKey() + "'. Field with same name was already registered");
                    }
                }
            }
        }
        return res;
    }

    private void buildFieldDictionary() {
        fieldsDictionary = listFields(cls, "", new ArrayList<java.lang.reflect.Field>());
        fieldList = new ArrayList<FieldInfo>(fieldsDictionary.values());
    }

    public String[] getHeader() {
        String[] header = new String[fieldList.size()];
        for (int i = 0, fieldListSize = fieldList.size(); i < fieldListSize; i++) {
            FieldInfo f = fieldList.get(i);
            header[i] = f.name;
        }
        return header;
    }

    private class FieldInfo {
        private List<java.lang.reflect.Field> fieldChain;
        private String name;
        private DataType type;

        private FieldInfo(String name, DataType type, List<java.lang.reflect.Field> fieldChain) {
            this.name = name;
            this.type = type;
            this.fieldChain = fieldChain;
        }

        public void set(Object instance, Object value) {
            for (Iterator<java.lang.reflect.Field> iterator = fieldChain.iterator(); iterator.hasNext();) {
                java.lang.reflect.Field f = iterator.next();
                try {
                    if (!iterator.hasNext()) {
                        f.set(instance, value);
                    } else {
                        Object newInstance = f.get(instance);
                        if (newInstance == null) {
                            newInstance = newInstance(f.getType());
                            f.set(instance, newInstance);
                        }
                        instance = newInstance;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }

        public Object get(Object instance) {
            for (Iterator<java.lang.reflect.Field> iterator = fieldChain.iterator(); iterator.hasNext();) {
                java.lang.reflect.Field f = iterator.next();
                try {
                    if (instance == null) {
                        return null;
                    }
                    if (!iterator.hasNext()) {
                        return f.get(instance);
                    } else {
                        instance = f.get(instance);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            //never happens
            throw new Error();
        }

        public String toString() {
            return name;
        }

    }


    public T parse(String str) {
        T instance;
        try {
            instance = newInstance(cls);
        } catch (Exception e) {
            throw new IllegalStateException("Class cls: " + cls.getName(), e);
        }
        int i = 0;
        for (String field : Splitter.on(separator).split(str)) {
            FieldInfo fieldInfo;
            try {
                fieldInfo = fieldList.get(i++);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalStateException("Number of fields in line exceeded number of fields in object (" + fieldList.size() + "). Line: '" + str + "'");
            }
            Object val = fieldInfo.type.parse(field);
            fieldInfo.set(instance, val);
        }
        if (i != fieldList.size()) {
            throw new IllegalStateException("Too few fields in line (" + i + "). It should be equal to fields in object (" + fieldList.size() + "). Line: '" + str + "'");
        }

        return instance;
    }

    private static <T> T newInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't instantiate " + cls.getName(), e);
        }
    }

    @Override
    public String toString(T object) {
        String[] fields = new String[fieldList.size()];
        for (int i = 0, allFieldsSize = fieldList.size(); i < allFieldsSize; i++) {
            FieldInfo f = fieldList.get(i);
            fields[i] = f.type.toString(f.get(object));
        }
        return Joiner.on(separator).join(fields);
    }

    @Override
    public String describe() {
        return cls.getSimpleName() + "Serializer";
    }

    /**
     * Returns all field of class and its superclasses
     *
     * @param cls class
     * @return all fields
     */
    private static List<java.lang.reflect.Field> getAllFields(Class cls) {
        List<java.lang.reflect.Field> r = new ArrayList<java.lang.reflect.Field>();
        while (cls != null) {
            r.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return r;
    }

    @Override
    public void setClass(Class cls) {
        this.cls = cls;
        buildFieldDictionary();
    }

    @Override
    public void setParams(String[] args) {
        this.separator = args[0];
    }
}
