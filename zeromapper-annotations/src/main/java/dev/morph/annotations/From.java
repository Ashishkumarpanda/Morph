package dev.morph.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a DTO field from a nested or renamed source property path.
 *
 * <p>Example: {@code @From("address.city") private String city;}
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface From {

    /**
     * Dot-separated property path on the source object.
     */
    String value();
}
