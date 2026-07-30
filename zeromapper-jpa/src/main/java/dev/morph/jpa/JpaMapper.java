package dev.morph.jpa;

import dev.morph.Mapper;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps JPA native query results and tuples to DTOs.
 */
public final class JpaMapper {

    private JpaMapper() {
    }

    public static <T> T fromTuple(Tuple tuple, Class<T> targetType) {
        Map<String, Object> row = new HashMap<>();
        for (TupleElement<?> element : tuple.getElements()) {
            String alias = element.getAlias();
            if (alias != null) {
                row.put(alias, tuple.get(alias));
            }
        }
        return Mapper.map(row, targetType);
    }

    public static <T> List<T> fromTuples(List<Tuple> tuples, Class<T> targetType) {
        return tuples.stream().map(tuple -> fromTuple(tuple, targetType)).toList();
    }

    public static <T> T fromRow(Object[] columns, String[] aliases, Class<T> targetType) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < columns.length && i < aliases.length; i++) {
            row.put(aliases[i], columns[i]);
        }
        return Mapper.map(row, targetType);
    }
}
