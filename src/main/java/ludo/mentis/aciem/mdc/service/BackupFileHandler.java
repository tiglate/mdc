package ludo.mentis.aciem.mdc.service;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Interface for handling backup file operations
 */
public interface BackupFileHandler {
    /**
     * Validates and resolves the given file path
     *
     * @param filePath the path to validate and resolve
     * @return the resolved path
     * @throws NoSuchFileException if the file doesn't exist
     * @throws IllegalArgumentException if the path is a directory
     */
    Path validateAndResolvePath(String filePath) throws NoSuchFileException;

    /**
     * Creates a backup path based on the source path and target date
     *
     * @param sourcePath the source file path
     * @param targetDate the target date for backup
     * @return the created backup path
     */
    Path createBackupPath(Path sourcePath, LocalDate targetDate);

    /**
     * Performs the actual backup operation
     *
     * @param sourcePath the source file path
     * @param targetPath the target backup path
     * @throws IOException if an I/O error occurs
     */
    void performBackup(Path sourcePath, Path targetPath) throws IOException;
}