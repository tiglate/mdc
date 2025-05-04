package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;
import ludo.mentis.aciem.mdc.model.Holiday;
import ludo.mentis.aciem.mdc.repository.HolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayManagerImplTest {

    @Mock
    private HolidayRepository holidayRepository;

    private HolidayManagerImpl holidayManager;

    @BeforeEach
    void setUp() {
        holidayManager = new HolidayManagerImpl(holidayRepository);
    }

    // Test data setup methods
    private List<Holiday> createTestHolidays() {
        return Arrays.asList(
                new Holiday(LocalDate.of(2025, 1, 1), "BRA", "Ano Novo"),
                new Holiday(LocalDate.of(2025, 4, 18), "BRA", "Paixão de Cristo"),
                new Holiday(LocalDate.of(2025, 12, 25), "BRA", "Natal"),
                new Holiday(LocalDate.of(2025, 1, 1), "USA", "New Year's Day"),
                new Holiday(LocalDate.of(2025, 7, 4), "USA", "Independence Day"),
                new Holiday(LocalDate.of(2025, 12, 25), "USA", "Christmas Day")
        );
    }

    @Test
    void initialize_shouldLoadHolidays_whenRepositoryReturnsData() throws HolidayLoadException {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);

        // When
        holidayManager.initialize();

        // Then
        verify(holidayRepository).findAll();
        // We can't directly verify holidaysByCountry as it's private,
        // but we can test its effects through calculateTargetDate
    }

    @Test
    void initialize_shouldThrowHolidayLoadException_whenRepositoryThrowsException() throws HolidayLoadException {
        // Given
        when(holidayRepository.findAll()).thenThrow(new HolidayLoadException("Test exception"));

        // When/Then
        assertThrows(HolidayLoadException.class, () -> holidayManager.initialize());
        verify(holidayRepository).findAll();
    }

    @Test
    void calculateTargetDate_shouldReturnYesterday_whenDaysBackIsOne_andConsiderBusinessDaysIsFalse() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();

        var today = LocalDate.now();
        var expected = today.minusDays(1);
        
        // When
        var result = holidayManager.calculateTargetDate(1, false, "BRA");
        
        // Then
        assertEquals(expected, result);
    }
    
    @Test
    void calculateTargetDate_shouldSkipWeekends_whenConsiderBusinessDaysIsTrue() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        // This test is more complex and depends on the current date
        // We'll need to handle different cases based on the current day of the week
        var today = LocalDate.now();
        var daysBack = 1;
        var result = holidayManager.calculateTargetDate(daysBack, true, "BRA");
        
        // The result should not be a weekend
        assertFalse(result.getDayOfWeek().getValue() >= 6, "Result date should not be a weekend");
        
        // The result should be before today
        assertTrue(result.isBefore(today), "Result date should be before today");
    }
    
    @Test
    void calculateTargetDate_shouldSkipHolidays_whenConsiderBusinessDaysIsTrue() throws Exception {
        // Given
        // Create holidays including today-1 as a holiday
        var today = LocalDate.now();
        var yesterday = today.minusDays(1);

        var holidays = List.of(
                new Holiday(yesterday, "BRA", "Test Holiday")
        );
        
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When
        var result = holidayManager.calculateTargetDate(1, true, "BRA");
        
        // Then
        // Result should not be yesterday (which is a holiday)
        assertNotEquals(yesterday, result);
        assertTrue(result.isBefore(yesterday), "Result should be before yesterday (the holiday)");
    }
    
    @Test
    void calculateTargetDate_shouldThrowException_whenDaysBackIsNegative() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> holidayManager.calculateTargetDate(-1, true, "BRA"));
    }
    
    @Test
    void calculateTargetDate_shouldThrowException_whenCountryCodeIsNull() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> holidayManager.calculateTargetDate(1, true, null));
    }
    
    @Test
    void calculateTargetDate_shouldThrowException_whenCountryCodeIsBlank() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
                () -> holidayManager.calculateTargetDate(1, true, ""));
    }
    
    @Test
    void calculateTargetDate_shouldThrowHolidaysNotAvailableException_whenCountryHolidaysNotLoaded() throws Exception {
        // Given
        var holidays = createTestHolidays(); // Only contains BRA and USA
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        assertThrows(HolidaysNotAvailableException.class, 
                () -> holidayManager.calculateTargetDate(1, true, "GBR"));
    }
    
    @Test
    void calculateTargetDate_shouldNotThrowException_whenCountryHolidaysNotLoaded_butConsiderBusinessDaysIsFalse() throws Exception {
        // Given
        var holidays = createTestHolidays(); // Only contains BRA and USA
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();
        
        // When/Then
        // Should not throw exception because we're not considering business days
        assertDoesNotThrow(() -> holidayManager.calculateTargetDate(1, false, "GBR"));
    }
    
    @Test
    void calculateTargetDate_shouldReturnCorrectDate_forMultipleDaysBack() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();

        var today = LocalDate.now();
        var expected = today.minusDays(5);
        
        // When
        var result = holidayManager.calculateTargetDate(5, false, "BRA");
        
        // Then
        assertEquals(expected, result);
    }


    @Test
    void getFormattedHolidays_shouldReturnFormattedHolidays_forSingleCountry() throws Exception {
        // Given
        var holidays = List.of(
                new Holiday(LocalDate.of(2025, 1, 1), "BRA", "Ano Novo"),
                new Holiday(LocalDate.of(2025, 12, 25), "BRA", "Natal")
        );
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();

        // When
        var result = holidayManager.getFormattedHolidays();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey("BRA"));
        assertEquals(Set.of("2025-01-01", "2025-12-25"), result.get("BRA"));
    }

    @Test
    void getFormattedHolidays_shouldReturnFormattedHolidays_forMultipleCountries() throws Exception {
        // Given
        var holidays = createTestHolidays();
        when(holidayRepository.findAll()).thenReturn(holidays);
        holidayManager.initialize();

        // When
        var result = holidayManager.getFormattedHolidays();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsKey("BRA"));
        assertTrue(result.containsKey("USA"));
        assertEquals(Set.of("2025-01-01", "2025-04-18", "2025-12-25"), result.get("BRA"));
        assertEquals(Set.of("2025-01-01", "2025-07-04", "2025-12-25"), result.get("USA"));
    }

    @Test
    void getFormattedHolidays_shouldReturnEmptyMap_whenNoHolidaysLoaded() throws Exception {
        // Given
        when(holidayRepository.findAll()).thenReturn(List.of());
        holidayManager.initialize();

        // When
        var result = holidayManager.getFormattedHolidays();

        // Then
        assertTrue(result.isEmpty());
    }

}