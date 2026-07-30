package dev.morph.metadata;

import dev.morph.exception.MorphException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates target instances using records, constructors, or default constructors.
 */
public final class InstanceFactory {

    private InstanceFactory() {
    }

    public static <T> T create(ClassMetadata metadata, Map<String, Object> values) {
        try {
            if (metadata.isRecord()) {
                return createRecord(metadata, values);
            }

            Constructor<?> constructor = metadata.preferredConstructor();
            if (constructor != null && constructor.getParameterCount() > 0) {
                return createWithConstructor(constructor, metadata.propertyMappings(), values);
            }

            T instance = instantiate(metadata.targetType());
            populateFields(instance, values);
            return instance;
        } catch (ReflectiveOperationException ex) {
            throw new MorphException("Failed to create instance of " + metadata.targetType().getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> type) throws ReflectiveOperationException {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }

    @SuppressWarnings("unchecked")
    private static <T> T createWithConstructor(
            Constructor<?> constructor,
            List<PropertyMapping> mappings,
            Map<String, Object> values
    ) throws ReflectiveOperationException {
        Object[] args = new Object[constructor.getParameterCount()];
        Map<String, Integer> indexByName = parameterIndex(constructor, mappings);

        for (PropertyMapping mapping : mappings) {
            Integer index = indexByName.get(mapping.targetName());
            if (index != null && values.containsKey(mapping.targetName())) {
                args[index] = values.get(mapping.targetName());
            }
        }

        return (T) constructor.newInstance(args);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createRecord(ClassMetadata metadata, Map<String, Object> values)
            throws ReflectiveOperationException {
        Constructor<?> constructor = metadata.preferredConstructor();
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];

        var components = metadata.targetType().getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            args[i] = values.get(components[i].getName());
        }

        return (T) constructor.newInstance(args);
    }

    private static Map<String, Integer> parameterIndex(Constructor<?> constructor, List<PropertyMapping> mappings) {
        Map<String, Integer> indexByName = new HashMap<>();
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            String name = "arg" + i;
            if (i < mappings.size()) {
                name = mappings.get(i).targetName();
            }
            indexByName.put(name, i);
        }
        return indexByName;
    }

    private static void populateFields(Object instance, Map<String, Object> values)
            throws IllegalAccessException {
        Class<?> type = instance.getClass();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Field field = findField(type, entry.getKey());
            if (field != null) {
                field.setAccessible(true);
                field.set(instance, entry.getValue());
            }
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
