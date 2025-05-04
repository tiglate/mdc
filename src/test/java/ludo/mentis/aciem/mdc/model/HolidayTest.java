package ludo.mentis.aciem.mdc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HolidayTest {

    @Test
    void constructor_shouldCreateHoliday_whenAllParametersAreValid() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var location = "BRA";
        var description = "New Year's Day";

        // When
        var holiday = new Holiday(date, location, description);

        // Then
        assertEquals(date, holiday.date());
        assertEquals(location, holiday.location());
        assertEquals(description, holiday.description());
    }

    @Test
    void constructor_shouldThrowNullPointerException_whenDateIsNull() {
        // Given
        var location = "BRA";
        var description = "New Year's Day";

        // When/Then
        var exception = assertThrows(NullPointerException.class, () -> new Holiday(null, location, description));
        assertEquals("date cannot be null", exception.getMessage());
    }

    @Test
    void constructor_shouldThrowNullPointerException_whenLocationIsNull() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var description = "New Year's Day";

        // When/Then
        var exception = assertThrows(NullPointerException.class, () -> new Holiday(date, null, description));
        assertEquals("location cannot be null", exception.getMessage());
    }

    @Test
    void constructor_shouldThrowNullPointerException_whenDescriptionIsNull() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var location = "BRA";

        // When/Then
        var exception = assertThrows(NullPointerException.class, () -> new Holiday(date, location, null));
        assertEquals("description cannot be null", exception.getMessage());
    }

    @Test
    void constructor_shouldNormalizeLocation_toUppercase() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var location = "bra";
        var description = "New Year's Day";

        // When
        var holiday = new Holiday(date, location, description);

        // Then
        assertEquals("BRA", holiday.location());
    }

    @Test
    void constructor_shouldTrimLocation() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var location = "  BRA  ";
        var description = "New Year's Day";

        // When
        var holiday = new Holiday(date, location, description);

        // Then
        assertEquals("BRA", holiday.location());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void constructor_shouldThrowIllegalArgumentException_whenLocationIsEmptyOrBlank(String location) {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var description = "New Year's Day";

        // When/Then
        var exception = assertThrows(IllegalArgumentException.class, () -> new Holiday(date, location, description));
        assertEquals("location cannot be empty", exception.getMessage());
    }

    @Test
    void record_shouldImplementEqualsAndHashCode() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var holiday1 = new Holiday(date, "BRA", "New Year's Day");
        var holiday2 = new Holiday(date, "BRA", "New Year's Day");
        var holiday3 = new Holiday(date, "USA", "New Year's Day");

        // Then
        assertEquals(holiday1, holiday2);
        assertNotEquals(holiday1, holiday3);
        assertEquals(holiday1.hashCode(), holiday2.hashCode());
    }

    @Test
    void record_shouldImplementToString() {
        // Given
        var date = LocalDate.of(2025, 1, 1);
        var holiday = new Holiday(date, "BRA", "New Year's Day");

        // When
        var toString = holiday.toString();

        // Then
        assertTrue(toString.contains("date=" + date));
        assertTrue(toString.contains("location=BRA"));
        assertTrue(toString.contains("description=New Year's Day"));
    }
}