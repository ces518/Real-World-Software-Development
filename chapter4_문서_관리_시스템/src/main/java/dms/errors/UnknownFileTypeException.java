package dms.errors;

public class UnknownFileTypeException extends RuntimeException {

    public UnknownFileTypeException() {
    }

    public UnknownFileTypeException(String message) {
        super(message);
    }

    public UnknownFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownFileTypeException(Throwable cause) {
        super(cause);
    }

    public UnknownFileTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
