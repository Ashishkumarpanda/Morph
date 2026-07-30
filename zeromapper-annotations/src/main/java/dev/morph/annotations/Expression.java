package dev.morph.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Computes a target field value from a SpEL-like expression over source properties.
 *
 * <p>Example: {@code @Expression("firstName + ' ' + lastName") private String fullName;}
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {

    /**
     * Expression evaluated against the source object.
     */
    String value();
}
