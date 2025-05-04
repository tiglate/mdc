package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;
import ludo.mentis.aciem.mdc.model.Holiday;
import ludo.mentis.aciem.mdc.repository.HolidayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HolidayManagerImpl implements HolidayManager {
    private static final Logger log = LoggerFactory.getLogger(HolidayManagerImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Map<String, Set<LocalDate>> holidaysByCountry = new ConcurrentHashMap<>();
    private final HolidayRepository holidayRepository;

    public HolidayManagerImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public void initialize() throws HolidayLoadException {
        try {
            processHolidays(holidayRepository.findAll());
            log.info("Holiday processing complete. {} countries loaded.", holidaysByCountry.size());
            if (log.isDebugEnabled()) {
                log.debug("Loaded holidays details: {}", getFormattedHolidays());
            }
        } catch (Exception e) {
            throw new HolidayLoadException("Failed to initialize holidays", e);
        }
    }

    private void processHolidays(List<Holiday> holidayList) {
        holidaysByCountry.clear();
        for (Holiday holiday : holidayList) {
            holidaysByCountry.computeIfAbsent(holiday.location(), k -> new HashSet<>())
                    .add(holiday.date());
        }
        log.info("Processed holidays into lookup map for countries: {}", holidaysByCountry.keySet());
    }

    @Override
    public LocalDate calculateTargetDate(int daysBack, boolean considerBusinessDays, String countryCode)
            throws HolidaysNotAvailableException {
        if (daysBack < 0) {
            throw new IllegalArgumentException("Days back must not be negative: %d".formatted(daysBack));
        }
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("Country code must not be null or blank");
        }

        var currentDate = LocalDate.now();
        var calculatedDate = currentDate;

        log.debug("Calculating target date starting from: {} (Context Date)", currentDate);

        var countryHolidays = holidaysByCountry.getOrDefault(countryCode, Collections.emptySet());
        validateHolidayAvailability(considerBusinessDays, countryCode);

        int validDaysSubtracted = 0;
        while (validDaysSubtracted < daysBack) {
            calculatedDate = calculatedDate.minusDays(1);
            if (isValidDay(calculatedDate, considerBusinessDays, countryHolidays)) {
                validDaysSubtracted++;
                log.trace("Counted {} day: {}", considerBusinessDays ? "business" : "calendar", calculatedDate);
            }
        }
        return calculatedDate;
    }

    private boolean isValidDay(LocalDate date, boolean considerBusinessDays, Set<LocalDate> countryHolidays) {
        if (!considerBusinessDays) {
            return true;
        }
        var isWeekend = isWeekend(date);
        var isHoliday = countryHolidays.contains(date);
        if (isWeekend || isHoliday) {
            log.trace("Skipping non-business day: {} (Weekend: {}, Holiday: {})", date, isWeekend, isHoliday);
            return false;
        }
        return true;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private void validateHolidayAvailability(boolean considerBusinessDays, String countryCode)
            throws HolidaysNotAvailableException {
        if (considerBusinessDays && !holidaysByCountry.containsKey(countryCode)) {
            throw new HolidaysNotAvailableException(("No holidays loaded for country code '%s'. " +
                    "Proceeding without specific holiday checks for this country.").formatted(countryCode));
        }
    }

    protected Map<String, Set<String>> getFormattedHolidays() {
        return holidaysByCountry.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(DATE_FORMATTER::format)
                                .collect(Collectors.toSet())
                ));
    }
}