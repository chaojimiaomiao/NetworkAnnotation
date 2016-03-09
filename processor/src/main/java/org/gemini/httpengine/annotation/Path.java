package org.gemini.httpengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by geminiwen on 15/5/20.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface Path {
    String value();
}
