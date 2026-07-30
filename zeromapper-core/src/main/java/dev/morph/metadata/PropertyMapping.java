package dev.morph.metadata;

/**
 * Describes how a single target property is populated from a source.
 */
public record PropertyMapping(
        String targetName,
        String sourcePath,
        Class<?> targetType,
        String expression
) {

    public boolean isComputed() {
        return expression != null && !expression.isBlank();
    }
}
