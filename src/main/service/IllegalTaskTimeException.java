package main.service;

public class IllegalTaskTimeException extends RuntimeException {
    public IllegalTaskTimeException() {
    }

    public IllegalTaskTimeException(String message) {
        super(message);
    }

    public IllegalTaskTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTaskTimeException(Throwable cause) {
        super(cause);
    }
}
