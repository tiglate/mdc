package ludo.mentis.aciem.mdc.exception;

/**
 * Checked exception thrown when there is an error loading holiday data.
 * This exception wraps lower-level exceptions that may occur during the holiday loading process.
 */
public class HolidayLoadException extends Exception {

    /**
     * Constructs a new HolidayLoadException with the specified detail message.
     *
     * @param message the detail message
     */
    public HolidayLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new HolidayLoadException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public HolidayLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
