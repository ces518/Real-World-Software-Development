package bank.errors;

public class InvalidDateFormat extends RuntimeException {

    public InvalidDateFormat() {
    }

    public InvalidDateFormat(String message) {
        super(message);
    }

    public InvalidDateFormat(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDateFormat(Throwable cause) {
        super(cause);
    }

    public InvalidDateFormat(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
