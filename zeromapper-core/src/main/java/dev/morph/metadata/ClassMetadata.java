package dev.morph.metadata;

import dev.morph.annotations.Expression;
import dev.morph.annotations.From;
import dev.morph.annotations.IgnoreMapping;
import dev.morph.annotations.MapperIgnoreNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached, immutable mapping metadata for a target class.
 */
public final class ClassMetadata {

    private static final Map<Class<?>, ClassMetadata> CACHE = new ConcurrentHashMap<>();

    private final Class<?> targetType;
    private final boolean ignoreNull;
    private final List<PropertyMapping> propertyMappings;
    private final Constructor<?> preferredConstructor;
    private final boolean record;

    private ClassMetadata(Class<?> targetType) {
        this.targetType = targetType;
        this.ignoreNull = targetType.isAnnotationPresent(MapperIgnoreNull.class);
        this.record = targetType.isRecord();
        this.propertyMappings = Collections.unmodifiableList(buildPropertyMappings(targetType));
        this.preferredConstructor = resolvePreferredConstructor(targetType, propertyMappings);
    }

    public static ClassMetadata of(Class<?> targetType) {
        return CACHE.computeIfAbsent(targetType, ClassMetadata::new);
    }

    public Class<?> targetType() {
        return targetType;
    }

    public boolean ignoreNull() {
        return ignoreNull;
    }

    public List<PropertyMapping> propertyMappings() {
        return propertyMappings;
    }

    Constructor<?> preferredConstructor() {
        return preferredConstructor;
    }

    public boolean isRecord() {
        return record;
    }

    private static List<PropertyMapping> buildPropertyMappings(Class<?> targetType) {
        Map<String, PropertyMapping> mappings = new LinkedHashMap<>();
        collectFields(targetType, mappings);
        return new ArrayList<>(mappings.values());
    }

    private static void collectFields(Class<?> type, Map<String, PropertyMapping> mappings) {
        if (type == null || type == Object.class) {
            return;
        }
        collectFields(type.getSuperclass(), mappings);

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.isAnnotationPresent(IgnoreMapping.class)) {
                continue;
            }
            if (mappings.containsKey(field.getName())) {
                continue;
            }

            String sourcePath = field.getName();
            From from = field.getAnnotation(From.class);
            if (from != null) {
                sourcePath = from.value();
            }

            Expression expression = field.getAnnotation(Expression.class);
            mappings.put(field.getName(), new PropertyMapping(
                    field.getName(),
                    sourcePath,
                    field.getType(),
                    expression != null ? expression.value() : null
            ));
        }
    }

    private static Constructor<?> resolvePreferredConstructor(Class<?> targetType, List<PropertyMapping> mappings) {
        if (targetType.isRecord()) {
            return targetType.getDeclaredConstructors()[0];
        }

        Constructor<?> best = null;
        int bestScore = -1;

        for (Constructor<?> constructor : targetType.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                if (best == null) {
                    best = constructor;
                    bestScore = 0;
                }
                continue;
            }

            int score = scoreConstructor(constructor, mappings);
            if (score > bestScore) {
                best = constructor;
                bestScore = score;
            }
        }

        if (best != null) {
            best.setAccessible(true);
        }
        return best;
    }

    private static int scoreConstructor(Constructor<?> constructor, List<PropertyMapping> mappings) {
        RecordComponent[] components = null;
        if (constructor.getDeclaringClass().isRecord()) {
            components = constructor.getDeclaringClass().getRecordComponents();
        }

        int score = 0;
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            String paramName = parameterName(constructor, i, components);
            for (PropertyMapping mapping : mappings) {
                if (mapping.targetName().equals(paramName)) {
                    score++;
                    break;
                }
            }
        }
        return score;
    }

    private static String parameterName(Constructor<?> constructor, int index, RecordComponent[] components) {
        if (components != null && index < components.length) {
            return components[index].getName();
        }
        return "arg" + index;
    }
}
