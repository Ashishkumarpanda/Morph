package dev.morph.access;

import dev.morph.exception.MorphException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes property values to target objects using cached MethodHandles.
 */
public final class PropertyWriter {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<String, MethodHandle> SETTER_CACHE = new ConcurrentHashMap<>();

    private PropertyWriter() {
    }

    public static void write(Object target, String property, Object value) {
        if (target == null) {
            return;
        }

        if (target instanceof Map<?, ?> map) {
            writeToMap((Map<Object, Object>) map, property, value);
            return;
        }

        MethodHandle setter = SETTER_CACHE.computeIfAbsent(
                cacheKey(target.getClass(), property),
                key -> resolveSetter(target.getClass(), property)
        );

        try {
            setter.invoke(target, value);
        } catch (Throwable ex) {
            throw new MorphException(
                    "Failed to write property '" + property + "' on " + target.getClass().getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeToMap(Map<Object, Object> map, String property, Object value) {
        if (property.contains(".")) {
            String[] parts = property.split("\\.", 2);
            Object nested = map.computeIfAbsent(parts[0], key -> new ConcurrentHashMap<>());
            if (nested instanceof Map<?, ?> nestedMap) {
                writeToMap((Map<Object, Object>) nestedMap, parts[1], value);
            }
            return;
        }
        map.put(property, value);
    }

    private static MethodHandle resolveSetter(Class<?> type, String property) {
        String capitalized = capitalize(property);

        try {
            Field field = findField(type, property);
            field.setAccessible(true);
            return LOOKUP.unreflectSetter(field);
        } catch (NoSuchFieldException ignored) {
            // fall through
        } catch (IllegalAccessException ex) {
            throw new MorphException("Cannot access field for " + property, ex);
        }

        for (Method method : type.getMethods()) {
            if (method.getName().equals("set" + capitalized) && method.getParameterCount() == 1) {
                method.setAccessible(true);
                try {
                    return LOOKUP.unreflect(method);
                } catch (IllegalAccessException ex) {
                    throw new MorphException("Cannot access setter for " + property, ex);
                }
            }
        }

        throw new MorphException("Writable property '" + property + "' not found on " + type.getName());
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
