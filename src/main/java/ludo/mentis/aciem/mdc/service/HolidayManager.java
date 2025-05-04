package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;

import java.time.LocalDate;

/**
 * Interface for managing holidays and date calculations
 */
public interface HolidayManager {
    /**
     * Initializes the holiday manager with data from the repository
     * @throws HolidayLoadException if there's an error loading holiday data
     */
    void initialize() throws HolidayLoadException;

    /**
     * Calculates the target date based on the given parameters
     *
     * @param daysBack number of days to go back
     * @param considerBusinessDays whether to skip weekends and holidays
     * @param countryCode the country code for holiday lookup
     * @return the calculated target date
     * @throws HolidaysNotAvailableException if holidays are not available for the given country
     */
    LocalDate calculateTargetDate(int daysBack, boolean considerBusinessDays, String countryCode)
            throws HolidaysNotAvailableException;
}