package bank.errors;

public class DescriptionTooLongException extends RuntimeException {

    public DescriptionTooLongException() {
    }

    public DescriptionTooLongException(String message) {
        super(message);
    }

    public DescriptionTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public DescriptionTooLongException(Throwable cause) {
        super(cause);
    }

    public DescriptionTooLongException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
