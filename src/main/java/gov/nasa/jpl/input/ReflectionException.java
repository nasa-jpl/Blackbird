package gov.nasa.jpl.input;

/**
 * Custom exception for reflection. Will be expanded in the future.
 */
public class ReflectionException extends Exception {
    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
