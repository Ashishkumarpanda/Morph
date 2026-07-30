package dev.morph.exception;

/**
 * Thrown when a source property path cannot be resolved.
 */
public class FieldNotFoundException extends MorphException {

    public FieldNotFoundException(String message) {
        super(message);
    }

    public FieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
