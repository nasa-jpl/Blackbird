package gov.nasa.jpl.engine;

/**
 * Custom exception for adaptation errors to prevent them from doing things the engine won't like but are syntactically correct Java.
 * Extends RuntimeException so adapters won't have to put try/catch clauses around all wait statements
 */
public class AdaptationException extends RuntimeException{
    public AdaptationException(String message) {
        super(message);
    }

    public AdaptationException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
