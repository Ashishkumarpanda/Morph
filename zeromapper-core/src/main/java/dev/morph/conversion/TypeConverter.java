package dev.morph.conversion;

import dev.morph.exception.TypeConversionException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Converts values between common Java types used in Spring applications.
 */
public final class TypeConverter {

    private TypeConverter() {
    }

    public static Object convert(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }

        if (targetType.isEnum() && value instanceof String stringValue) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumType, stringValue);
        }

        if (value instanceof Enum<?> enumValue) {
            if (targetType == String.class) {
                return enumValue.name();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return enumValue.ordinal();
            }
        }

        if (value instanceof Number number) {
            return convertNumber(number, targetType);
        }

        if (value instanceof String stringValue) {
            return convertString(stringValue, targetType);
        }

        if (value instanceof UUID uuid && targetType == String.class) {
            return uuid.toString();
        }

        if (value instanceof LocalDate localDate && targetType == LocalDateTime.class) {
            return localDate.atStartOfDay();
        }

        if (value instanceof LocalDateTime localDateTime && targetType == LocalDate.class) {
            return localDateTime.toLocalDate();
        }

        if (value instanceof Date date && targetType == Instant.class) {
            return date.toInstant();
        }

        if (value instanceof Instant instant && targetType == Date.class) {
            return Date.from(instant);
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        throw new TypeConversionException(
                "Cannot convert " + value.getClass().getName() + " to " + targetType.getName());
    }

    private static Object convertNumber(Number number, Class<?> targetType) {
        if (targetType == Integer.class || targetType == int.class) {
            return number.intValue();
        }
        if (targetType == Long.class || targetType == long.class) {
            return number.longValue();
        }
        if (targetType == Double.class || targetType == double.class) {
            return number.doubleValue();
        }
        if (targetType == Float.class || targetType == float.class) {
            return number.floatValue();
        }
        if (targetType == BigDecimal.class) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (targetType == String.class) {
            return number.toString();
        }
        throw new TypeConversionException(
                "Cannot convert Number to " + targetType.getName());
    }

    private static Object convertString(String value, Class<?> targetType) {
        if (targetType == UUID.class) {
            return UUID.fromString(value);
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value);
        }
        if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        }
        if (targetType == Instant.class) {
            return Instant.parse(value);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(value);
        }
        throw new TypeConversionException(
                "Cannot convert String to " + targetType.getName());
    }
}
