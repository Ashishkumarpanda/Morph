package dev.morph.spring;

import dev.morph.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Spring-friendly injectable mapper facade.
 */
public class MorphMapper {

    public <S, T> T map(S source, Class<T> targetType) {
        return Mapper.map(source, targetType);
    }

    public <S, T> T map(S source, T target) {
        return Mapper.map(source, target);
    }

    public <S, T> List<T> list(Collection<S> source, Class<T> targetType) {
        return Mapper.list(source, targetType);
    }

    public <S, T> Set<T> set(Collection<S> source, Class<T> targetType) {
        return Mapper.set(source, targetType);
    }

    public <S, T> Stream<T> stream(Stream<S> source, Class<T> targetType) {
        return Mapper.stream(source, targetType);
    }

    public <S, T> Page<T> page(Page<S> source, Class<T> targetType) {
        List<T> content = list(source.getContent(), targetType);
        return new PageImpl<>(content, source.getPageable(), source.getTotalElements());
    }
}
