package bank.errors;

public class DateInTheFutureException extends RuntimeException {

    public DateInTheFutureException() {
    }

    public DateInTheFutureException(String message) {
        super(message);
    }

    public DateInTheFutureException(String message, Throwable cause) {
        super(message, cause);
    }

    public DateInTheFutureException(Throwable cause) {
        super(cause);
    }

    public DateInTheFutureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
