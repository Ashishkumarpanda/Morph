package dev.morph.exception;

/**
 * Base runtime exception for Morph mapping failures.
 */
public class MorphException extends RuntimeException {

    public MorphException(String message) {
        super(message);
    }

    public MorphException(String message, Throwable cause) {
        super(message, cause);
    }
}
