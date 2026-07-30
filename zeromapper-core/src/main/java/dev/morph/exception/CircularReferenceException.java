package dev.morph.exception;

/**
 * Thrown when a circular object graph is detected during mapping.
 */
public class CircularReferenceException extends MorphException {

    public CircularReferenceException(String message) {
        super(message);
    }
}
