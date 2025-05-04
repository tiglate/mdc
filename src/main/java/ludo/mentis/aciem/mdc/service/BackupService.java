package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

/**
 * Interface for services that handle file backups according to specific date rules.
 */
public interface BackupService {

    /**
     * Backs up the file to the last business day before today, using default country holidays ("BRA").
     *
     * @param filePath The full path to the file to be backed up.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws NoSuchFileException If the source file does not exist.
     * @throws IllegalArgumentException if filePath represents a directory.
     * @throws HolidaysNotAvailableException if the country holidays cannot be loaded for the specified country.
     */
    void backup(String filePath) throws IOException, HolidaysNotAvailableException;

    /**
     * Backs up the file to n days before today, considering only business days, using default country holidays ("BRA").
     *
     * @param filePath The full path to the file to be backed up.
     * @param daysBack The number of business days to go back (1 = last business day). Must be > 0.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws NoSuchFileException If the source file does not exist.
     * @throws IllegalArgumentException if daysBack is not positive or if filePath represents a directory.
     * @throws HolidaysNotAvailableException if the country holidays cannot be loaded for the specified country.
     */
    void backup(String filePath, int daysBack) throws IOException, HolidaysNotAvailableException;

    /**
     * Backs up the file to n days before today, optionally considering only business days, using default country
     * holidays ("BRA").
     *
     * @param filePath             The full path to the file to be backed up.
     * @param daysBack             The number of days to go back (1 = yesterday or last valid day). Must be > 0.
     * @param considerBusinessDays If true, skip weekends and holidays. If false, simply subtract days.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws NoSuchFileException If the source file does not exist.
     * @throws IllegalArgumentException if daysBack is not positive or if filePath represents a directory.
     * @throws HolidaysNotAvailableException if the country holidays cannot be loaded for the specified country.
     */
    void backup(String filePath, int daysBack, boolean considerBusinessDays) throws IOException, HolidaysNotAvailableException;

    /**
     * Backs up the file to the last business day before today, using specified country holidays.
     *
     * @param filePath    The full path to the file to be backed up.
     * @param countryCode The country code (e.g., "BRA", "USA") for holiday lookup. Case-insensitive.
     * @throws IOException If an I/O error occurs during file operations.
     * @throws NoSuchFileException If the source file does not exist.
     * @throws IllegalArgumentException if filePath represents a directory.
     * @throws HolidaysNotAvailableException if the country holidays cannot be loaded for the specified country.
     */
    void backup(String filePath, String countryCode) throws IOException, HolidaysNotAvailableException;

    /**
     * Core backup method. Backs up the file by moving it to a subdirectory structure
     * based on a calculated date relative to today.
     *
     * @param filePath             The full path to the file to be backed up.
     * @param daysBack             The number of days to go back (1 = yesterday or last valid day). Must be > 0.
     * @param considerBusinessDays If true, skip weekends and holidays for the specified country. If false, simply
     *                             subtract days.
     * @param countryCode          The country code (e.g., "BRA", "USA") for holiday lookup if considering business
     *                             days. Case-insensitive. Defaults to "BRA" if null or empty and considerBusinessDays
     *                             is true.
     * @throws IOException If an I/O error occurs during file creation or move.
     * @throws NoSuchFileException If the source file does not exist.
     * @throws IllegalArgumentException if daysBack is not positive or if filePath represents a directory.
     * @throws HolidaysNotAvailableException if the country holidays cannot be loaded for the specified country.
     */
    void backup(String filePath, int daysBack, boolean considerBusinessDays, String countryCode) throws IOException, HolidaysNotAvailableException;
}