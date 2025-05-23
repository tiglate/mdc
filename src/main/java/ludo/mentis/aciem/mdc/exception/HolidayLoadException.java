package ludo.mentis.aciem.mdc.exception;

import org.springframework.aot.generate.Generated;

import java.io.Serial;

/**
 * Checked exception thrown when there is an error loading holiday data.
 * This exception wraps lower-level exceptions that may occur during the holiday loading process.
 */
@Generated
public class HolidayLoadException extends Exception {

	@Serial
    private static final long serialVersionUID = 6090794116698802359L;

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
