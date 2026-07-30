package dev.morph.expression;

import dev.morph.access.PropertyReader;

/**
 * Minimal expression evaluator for {@code @Expression} fields.
 *
 * <p>Supports simple concatenation: {@code firstName + ' ' + lastName}
 */
public final class ExpressionEvaluator {

    private ExpressionEvaluator() {
    }

    public static Object evaluate(Object source, String expression) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        String trimmed = expression.trim();
        if (trimmed.contains("+")) {
            String[] parts = trimmed.split("\\+");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                builder.append(resolvePart(source, part.trim()));
            }
            return builder.toString();
        }

        return resolvePart(source, trimmed);
    }

    private static Object resolvePart(Object source, String part) {
        if ((part.startsWith("'") && part.endsWith("'"))
                || (part.startsWith("\"") && part.endsWith("\""))) {
            return part.substring(1, part.length() - 1);
        }
        return PropertyReader.read(source, part);
    }
}
