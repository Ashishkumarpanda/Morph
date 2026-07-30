package dev.morph;

import dev.morph.engine.DefaultMappingEngine;
import dev.morph.engine.MappingEngine;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Entry point for Morph object mapping.
 *
 * <pre>{@code
 * UserDto dto = Mapper.map(user, UserDto.class);
 * List<UserDto> dtos = Mapper.list(users, UserDto.class);
 * }</pre>
 */
public final class Mapper {

    private static volatile MappingEngine engine = DefaultMappingEngine.getInstance();

    private Mapper() {
    }

    /**
     * Maps a source object to a new instance of the target type.
     */
    public static <S, T> T map(S source, Class<T> targetType) {
        return engine.map(source, targetType);
    }

    /**
     * Maps a source object into an existing target instance.
     */
    public static <S, T> T map(S source, T target) {
        return engine.map(source, target);
    }

    /**
     * Maps a collection to a list of target type instances.
     */
    public static <S, T> List<T> list(Collection<S> source, Class<T> targetType) {
        return engine.list(source, targetType);
    }

    /**
     * Maps a collection to a set of target type instances.
     */
    public static <S, T> Set<T> set(Collection<S> source, Class<T> targetType) {
        return engine.set(source, targetType);
    }

    /**
     * Maps elements of a stream to the target type.
     */
    public static <S, T> Stream<T> stream(Stream<S> source, Class<T> targetType) {
        return engine.stream(source, targetType);
    }

    /**
     * Replaces the global mapping engine (used by Spring auto-configuration).
     */
    public static void useEngine(MappingEngine mappingEngine) {
        engine = Objects.requireNonNull(mappingEngine, "mappingEngine");
    }
}
