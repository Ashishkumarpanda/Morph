package dev.morph.exception;

/**
 * Thrown when a value cannot be converted to the target field type.
 */
public class TypeConversionException extends MorphException {

    public TypeConversionException(String message) {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
