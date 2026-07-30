package dev.morph.access;

import dev.morph.exception.FieldNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads property values from arbitrary source objects using cached MethodHandles.
 */
public final class PropertyReader {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<String, MethodHandle> GETTER_CACHE = new ConcurrentHashMap<>();

    private PropertyReader() {
    }

    public static Object read(Object source, String path) {
        if (source == null || path == null || path.isBlank()) {
            return null;
        }

        if (source instanceof Map<?, ?> map) {
            return readFromMap(map, path);
        }

        Object current = source;
        for (String segment : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = readSegment(current, segment);
        }
        return current;
    }

    private static Object readFromMap(Map<?, ?> map, String path) {
        Object current = map;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(segment);
        }
        return current;
    }

    private static Object readSegment(Object source, String property) {
        Class<?> type = source.getClass();

        if (source instanceof Map<?, ?> map) {
            return map.get(property);
        }

        MethodHandle getter = GETTER_CACHE.computeIfAbsent(
                cacheKey(type, property),
                key -> resolveGetter(type, property)
        );

        try {
            return getter.invoke(source);
        } catch (Throwable ex) {
            if (ex.getCause() instanceof FieldNotFoundException fieldNotFound) {
                throw fieldNotFound;
            }
            throw new FieldNotFoundException(
                    "Failed to read property '" + property + "' from " + type.getName() + ": " + ex.getMessage(), ex);
        }
    }

    public static Object readOrNull(Object source, String path) {
        try {
            return read(source, path);
        } catch (FieldNotFoundException ex) {
            return null;
        }
    }

    private static MethodHandle resolveGetter(Class<?> type, String property) {
        String capitalized = capitalize(property);

        try {
            Method getter = type.getMethod("get" + capitalized);
            getter.setAccessible(true);
            return LOOKUP.unreflect(getter);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
            // fall through
        }

        try {
            Method getter = type.getMethod("is" + capitalized);
            getter.setAccessible(true);
            return LOOKUP.unreflect(getter);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
            // fall through
        }

        try {
            Field field = findField(type, property);
            field.setAccessible(true);
            return LOOKUP.unreflectGetter(field);
        } catch (NoSuchFieldException ex) {
            throw new FieldNotFoundException(
                    "Property '" + property + "' not found on " + type.getName());
        } catch (IllegalAccessException ex) {
            throw new FieldNotFoundException(
                    "Cannot access property '" + property + "' on " + type.getName(), ex);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static String cacheKey(Class<?> type, String property) {
        return type.getName() + "#" + property;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
