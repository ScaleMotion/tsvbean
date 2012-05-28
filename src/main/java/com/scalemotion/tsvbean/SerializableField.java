package com.scalemotion.tsvbean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializableField {
    public String name() default "";
    public Class<? extends DataType> type() default DataType.class;
    public boolean required() default false;
    public String[] args() default {};
}
