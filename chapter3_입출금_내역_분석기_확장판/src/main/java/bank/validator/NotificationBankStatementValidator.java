package bank.validator;

import bank.errors.DateInTheFutureException;
import bank.errors.DescriptionTooLongException;
import bank.errors.InvalidAmountException;
import bank.errors.InvalidDateFormat;
import bank.notification.Notification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class NotificationBankStatementValidator {

    private String description;
    private String date;
    private String amount;

    public NotificationBankStatementValidator(final String description, final String date, final String amount) {
        this.description = description;
        this.date = date;
        this.amount = amount;
    }

    public Notification validate() {
        final Notification notification = new Notification();
        if (this.description.length() > 100) {
            notification.addError("The description is too long");
        }

        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(this.date);

            if (parsedDate.isAfter(LocalDate.now())) {
                notification.addError("date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            notification.addError("Invalid format for date");
        }


        try {
            Double.parseDouble(this.amount);
        } catch (NumberFormatException e) {
            notification.addError("Invalid format for amount");
        }

        return notification;
    }
}
