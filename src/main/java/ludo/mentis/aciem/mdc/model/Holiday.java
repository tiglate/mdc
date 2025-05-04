package ludo.mentis.aciem.mdc.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a holiday entry.
 *
 * @param date        The date of the holiday.
 * @param location    The country code (e.g., "BRA", "USA") or region where the holiday applies.
 * @param description A description of the holiday.
 */
public record Holiday(LocalDate date, String location, String description) {

    /**
     * Canonical constructor with validation/normalization.
     */
    public Holiday {
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(location, "location cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
        // Normalize location to uppercase for consistent lookups
        location = location.trim().toUpperCase();
        if (location.isEmpty()) {
            throw new IllegalArgumentException("location cannot be empty");
        }
    }
}