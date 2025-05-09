package ludo.mentis.aciem.mdc.exception;

import org.springframework.aot.generate.Generated;

import java.io.Serial;

/**
 * Exception thrown when a file has an invalid format.
 * This is used when parsing files that don't conform to the expected structure.
 */
@Generated
public class InvalidFileFormatException extends Exception {

	@Serial
    private static final long serialVersionUID = -411975825191539596L;

	/**
     * Constructs a new InvalidFileFormatException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidFileFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidFileFormatException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}