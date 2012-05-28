package com.scalemotion.tsvbean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EmbeddedDataField {
    public static final String INHERIT_PREFIX = "__$$INHERIT_PREFIX$$_";
    public String fieldPrefix() default INHERIT_PREFIX;
}
