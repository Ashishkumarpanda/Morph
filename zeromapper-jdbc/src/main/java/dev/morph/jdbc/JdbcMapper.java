package dev.morph.jdbc;

import dev.morph.Mapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JDBC query helpers that map rows directly to DTOs without RowMapper boilerplate.
 */
public final class JdbcMapper {

    private JdbcMapper() {
    }

    public static <T> List<T> query(JdbcTemplate jdbcTemplate, String sql, Class<T> targetType) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(toPropertyName(meta.getColumnLabel(i)), rs.getObject(i));
            }
            return Mapper.map(row, targetType);
        });
    }

    public static <T> List<T> query(
            NamedParameterJdbcTemplate jdbcTemplate,
            String sql,
            Map<String, ?> params,
            Class<T> targetType
    ) {
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(toPropertyName(meta.getColumnLabel(i)), rs.getObject(i));
            }
            return Mapper.map(row, targetType);
        });
    }

    /**
     * Maps a native query result row represented as {@code Object[]} into a DTO.
     */
    public static <T> T fromRow(Object[] columns, String[] aliases, Class<T> targetType) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < columns.length && i < aliases.length; i++) {
            row.put(toPropertyName(aliases[i]), columns[i]);
        }
        return Mapper.map(row, targetType);
    }

    private static String toPropertyName(String column) {
        if (column == null) {
            return null;
        }
        String normalized = column.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        StringBuilder builder = new StringBuilder();
        boolean upperNext = false;
        for (char ch : normalized.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                builder.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}
