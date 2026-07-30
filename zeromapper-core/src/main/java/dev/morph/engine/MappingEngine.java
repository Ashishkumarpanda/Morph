package dev.morph.engine;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Core mapping contract implemented by the Morph engine.
 */
public interface MappingEngine {

    <S, T> T map(S source, Class<T> targetType);

    <S, T> T map(S source, T target);

    <S, T> List<T> list(Collection<S> source, Class<T> targetType);

    <S, T> Set<T> set(Collection<S> source, Class<T> targetType);

    <S, T> Stream<T> stream(Stream<S> source, Class<T> targetType);
}
