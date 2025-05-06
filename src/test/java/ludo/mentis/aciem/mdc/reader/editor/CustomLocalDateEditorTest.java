package ludo.mentis.aciem.mdc.reader.editor;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CustomLocalDateEditorTest {

    @Test
    void shouldParseValidDateWithPattern() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        var dateString = "05/15/2023";
        
        // When
        editor.setAsText(dateString);
        
        // Then
        var result = editor.getValue();
        assertNotNull(result);
        assertTrue(result instanceof LocalDate);
        assertEquals(LocalDate.of(2023, 5, 15), result);
    }

    @Test
    void shouldParseValidDateWithFormatter() {
        // Given
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var editor = new CustomLocalDateEditor(formatter);
        var dateString = "2023-05-15";
        
        // When
        editor.setAsText(dateString);
        
        // Then
        var result = editor.getValue();
        assertNotNull(result);
        assertTrue(result instanceof LocalDate);
        assertEquals(LocalDate.of(2023, 5, 15), result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void shouldHandleEmptyOrBlankInput(String input) {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        
        // When
        editor.setAsText(input);
        
        // Then
        assertNull(editor.getValue());
    }

    @Test
    void shouldHandleNullInput() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        
        // When
        editor.setAsText(null);
        
        // Then
        assertNull(editor.getValue());
    }

    @Test
    void shouldThrowExceptionForInvalidDateFormat() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        var invalidDateString = "2023-05-15"; // Wrong format
        
        // When/Then
        assertThrows(DateTimeParseException.class, () -> editor.setAsText(invalidDateString));
    }

    @Test
    void shouldFormatDateAsText() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        var date = LocalDate.of(2023, 5, 15);
        editor.setValue(date);
        
        // When
        var result = editor.getAsText();
        
        // Then
        assertEquals("05/15/2023", result);
    }

    @Test
    void shouldReturnEmptyStringForNullValue() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        editor.setValue(null);
        
        // When
        var result = editor.getAsText();
        
        // Then
        assertEquals("", result);
    }

    @Test
    void shouldSupportDifferentDateFormats() {
        // Given
        var formats = new String[][] {
            {"yyyy-MM-dd", "2023-05-15"},
            {"dd/MM/yyyy", "15/05/2023"},
            {"MM-dd-yyyy", "05-15-2023"},
            {"yyyyMMdd", "20230515"}
        };
        
        for (var format : formats) {
            var pattern = format[0];
            var dateString = format[1];
            var editor = new CustomLocalDateEditor(pattern);
            
            // When
            editor.setAsText(dateString);
            
            // Then
            var result = editor.getValue();
            assertNotNull(result);
            assertTrue(result instanceof LocalDate);
            assertEquals(LocalDate.of(2023, 5, 15), result);
        }
    }

    @Test
    void shouldRoundTripDateConversion() {
        // Given
        var editor = new CustomLocalDateEditor("yyyy-MM-dd");
        var originalDate = "2023-05-15";
        
        // When
        editor.setAsText(originalDate);
        var formattedDate = editor.getAsText();
        
        // Then
        assertEquals(originalDate, formattedDate);
    }

    @Test
    void shouldHandleLeapYearDates() {
        // Given
        var editor = new CustomLocalDateEditor("MM/dd/yyyy");
        var leapYearDate = "02/29/2024"; // 2024 is a leap year
        
        // When
        editor.setAsText(leapYearDate);
        
        // Then
        var result = editor.getValue();
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }
}