package ludo.mentis.aciem.mdc.exception;

/**
 * Runtime exception thrown when a backup operation fails.
 * This exception is used to indicate issues encountered during
 * the backup process and provides relevant error messaging.
 */
public class BackupException extends RuntimeException {

    /**
     * Constructs a new BackupException with the specified detail message.
     *
     * @param message the detail message describing the reason for the backup failure
     */
    public BackupException(String message) {
        super(message);
    }

    /**
     * Constructs a new BackupException with the specified detail message and cause.
     *
     * @param message the detail message describing the reason for the backup failure
     * @param cause the cause of the exception, which can be used to trace the root issue
     */
    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}
