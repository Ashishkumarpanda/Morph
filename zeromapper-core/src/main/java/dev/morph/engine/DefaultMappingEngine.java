package dev.morph.engine;

import dev.morph.access.PropertyReader;
import dev.morph.access.PropertyWriter;
import dev.morph.conversion.TypeConverter;
import dev.morph.exception.CircularReferenceException;
import dev.morph.expression.ExpressionEvaluator;
import dev.morph.metadata.ClassMetadata;
import dev.morph.metadata.InstanceFactory;
import dev.morph.metadata.PropertyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Default thread-safe mapping engine with cached metadata and MethodHandle access.
 */
public final class DefaultMappingEngine implements MappingEngine {

    private static final DefaultMappingEngine INSTANCE = new DefaultMappingEngine();
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMappingEngine.class);

    private DefaultMappingEngine() {
    }

    public static MappingEngine getInstance() {
        return INSTANCE;
    }

    @Override
    public <S, T> T map(S source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        if (targetType.isInstance(source)) {
            @SuppressWarnings("unchecked")
            T cast = (T) source;
            return cast;
        }

        ClassMetadata metadata = ClassMetadata.of(targetType);
        Map<String, Object> values = extractValues(source, metadata, new IdentityHashMap<>());
        return InstanceFactory.create(metadata, values);
    }

    @Override
    public <S, T> T map(S source, T target) {
        if (source == null || target == null) {
            return target;
        }

        ClassMetadata metadata = ClassMetadata.of(target.getClass());
        Map<String, Object> values = extractValues(source, metadata, new IdentityHashMap<>());
        for (PropertyMapping mapping : metadata.propertyMappings()) {
            if (!values.containsKey(mapping.targetName())) {
                continue;
            }
            Object value = values.get(mapping.targetName());
            if (metadata.ignoreNull() && value == null) {
                continue;
            }
            PropertyWriter.write(target, mapping.targetName(), value);
        }
        return target;
    }

    @Override
    public <S, T> List<T> list(Collection<S> source, Class<T> targetType) {
        if (source == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>(source.size());
        for (S item : source) {
            result.add(map(item, targetType));
        }
        return result;
    }

    @Override
    public <S, T> Set<T> set(Collection<S> source, Class<T> targetType) {
        if (source == null) {
            return Set.of();
        }
        Set<T> result = new LinkedHashSet<>(source.size());
        for (S item : source) {
            result.add(map(item, targetType));
        }
        return result;
    }

    @Override
    public <S, T> Stream<T> stream(Stream<S> source, Class<T> targetType) {
        if (source == null) {
            return Stream.empty();
        }
        return source.map(item -> map(item, targetType));
    }

    private Map<String, Object> extractValues(
            Object source,
            ClassMetadata metadata,
            Map<Object, Boolean> visited
    ) {
        if (visited.containsKey(source)) {
            throw new CircularReferenceException(
                    "Circular reference detected while mapping " + source.getClass().getName());
        }
        visited.put(source, Boolean.TRUE);

        Map<String, Object> values = new java.util.LinkedHashMap<>();
        for (PropertyMapping mapping : metadata.propertyMappings()) {
            Object rawValue = mapping.isComputed()
                    ? ExpressionEvaluator.evaluate(source, mapping.expression())
                    : PropertyReader.readOrNull(source, mapping.sourcePath());

            if (metadata.ignoreNull() && rawValue == null) {
                continue;
            }

            Object converted = convertValue(rawValue, mapping.targetType(), visited);
            values.put(mapping.targetName(), converted);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapped {} -> {} ({})",
                        mapping.sourcePath(), mapping.targetName(), mapping.targetType().getSimpleName());
            }
        }

        visited.remove(source);
        return values;
    }

    private Object convertValue(Object value, Class<?> targetType, Map<Object, Boolean> visited) {
        if (value == null) {
            return null;
        }

        if (isSimpleType(targetType)) {
            return TypeConverter.convert(value, targetType);
        }

        if (value instanceof Collection<?> collection && !Collection.class.isAssignableFrom(targetType)) {
            return mapCollection(collection, targetType, visited);
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        if (isComplexType(targetType)) {
            ClassMetadata nestedMetadata = ClassMetadata.of(targetType);
            Map<String, Object> nestedValues = extractValues(value, nestedMetadata, visited);
            return InstanceFactory.create(nestedMetadata, nestedValues);
        }

        return TypeConverter.convert(value, targetType);
    }

    private Object mapCollection(Collection<?> collection, Class<?> targetType, Map<Object, Boolean> visited) {
        ClassMetadata metadata = ClassMetadata.of(targetType);
        List<Object> mapped = new ArrayList<>(collection.size());
        for (Object item : collection) {
            if (item == null) {
                mapped.add(null);
            } else if (isSimpleType(item.getClass())) {
                mapped.add(TypeConverter.convert(item, metadata.targetType()));
            } else {
                Map<String, Object> values = extractValues(item, metadata, visited);
                mapped.add(InstanceFactory.create(metadata, values));
            }
        }
        return mapped;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || type.isEnum()
                || type == UUID.class
                || type.getPackageName().startsWith("java.time");
    }

    private static boolean isComplexType(Class<?> type) {
        return !type.isPrimitive()
                && !type.getName().startsWith("java.")
                && !type.isEnum();
    }
}
