package ludo.mentis.aciem.mdc.repository;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.model.Holiday;

import java.util.List;

/**
 * Repository interface for accessing holiday data.
 */
public interface HolidayRepository {

    /**
     * Loads all holidays from the data source.
     *
     * @return A list of Holiday objects.
     * @throws HolidayLoadException if there is an error loading the holidays.
     */
    List<Holiday> findAll() throws HolidayLoadException;
}
