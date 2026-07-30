package dev.morph.test;

import dev.morph.Mapper;

import java.lang.reflect.Field;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion helpers for mapping tests.
 */
public final class MorphAssertions {

    private MorphAssertions() {
    }

    public static <S, T> void assertMapsTo(S source, Class<T> targetType, String... fieldNames) {
        T mapped = Mapper.map(source, targetType);
        assertThat(mapped).isNotNull();

        for (String fieldName : fieldNames) {
            Object sourceValue = readField(source, fieldName);
            Object targetValue = readField(mapped, fieldName);
            assertThat(targetValue).isEqualTo(sourceValue);
        }
    }

    private static Object readField(Object instance, String name) {
        Objects.requireNonNull(instance, "instance");
        Class<?> type = instance.getClass();
        while (type != null && type != Object.class) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Cannot read field " + name, ex);
            }
        }
        throw new IllegalArgumentException("Field not found: " + name);
    }
}
