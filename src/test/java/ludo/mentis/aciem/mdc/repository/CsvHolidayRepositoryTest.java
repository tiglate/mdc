package ludo.mentis.aciem.mdc.repository;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.InvalidFileFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CsvHolidayRepositoryTest {

    private TestCsvHolidayRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestCsvHolidayRepository();
    }

    // Test class that extends CsvHolidayRepository to allow testing with custom input streams
    private static class TestCsvHolidayRepository extends CsvHolidayRepository {
        private String csvContent;

        public void setCsvContent(String csvContent) {
            this.csvContent = csvContent;
        }

        @Override
        protected java.io.InputStream getHolidayFileStream() throws IOException {
            if (csvContent == null) {
                throw new IOException("holidays.csv not found in classpath");
            }
            return new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void shouldProcessLine_shouldReturnTrue_forValidLine() {
        // Given
        var line = "2025-01-01,BRA,Ano Novo";

        // When
        var result = repository.shouldProcessLine(line);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldProcessLine_shouldReturnFalse_forEmptyLine() {
        // Given
        var line = "";

        // When
        var result = repository.shouldProcessLine(line);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldProcessLine_shouldReturnFalse_forCommentLine() {
        // Given
        var line = "# This is a comment";

        // When
        var result = repository.shouldProcessLine(line);

        // Then
        assertFalse(result);
    }

    @Test
    void parseLine_shouldReturnHoliday_forValidLine() throws InvalidFileFormatException {
        // Given
        var line = "2025-01-01,BRA,Ano Novo";
        var lineNum = 2;

        // When
        var holiday = repository.parseLine(line, lineNum);

        // Then
        assertEquals(LocalDate.of(2025, 1, 1), holiday.date());
        assertEquals("BRA", holiday.location());
        assertEquals("Ano Novo", holiday.description());
    }

    @Test
    void parseLine_shouldThrowException_forInsufficientColumns() {
        // Given
        var line = "2025-01-01,BRA";
        var lineNum = 2;

        // When/Then
        var exception = assertThrows(InvalidFileFormatException.class, () -> repository.parseLine(line, lineNum));
        assertTrue(exception.getMessage().contains("Insufficient columns"));
    }

    @Test
    void parseLine_shouldThrowException_forInvalidDate() {
        // Given
        var line = "invalid-date,BRA,Ano Novo";
        var lineNum = 2;

        // When/Then
        var exception = assertThrows(InvalidFileFormatException.class, () -> repository.parseLine(line, lineNum));
        assertTrue(exception.getMessage().contains("Invalid date format"));
    }

    @Test
    void validateHeader_shouldNotThrowException_forValidHeader() {
        // Given
        var reader = new BufferedReader(new StringReader("Date,Location,Description"));

        // When/Then
        assertDoesNotThrow(() -> repository.validateHeader(reader));
    }

    @Test
    void validateHeader_shouldThrowException_forEmptyFile() {
        // Given
        var reader = new BufferedReader(new StringReader(""));

        // When/Then
        var exception = assertThrows(InvalidFileFormatException.class, () -> repository.validateHeader(reader));
        assertTrue(exception.getMessage().contains("is empty"));
    }

    @Test
    void validateHeader_shouldThrowException_forInvalidHeader() {
        // Given
        var reader = new BufferedReader(new StringReader("Invalid,Header"));

        // When/Then
        var exception = assertThrows(InvalidFileFormatException.class, () -> repository.validateHeader(reader));
        assertTrue(exception.getMessage().contains("invalid header"));
    }

    @Test
    void processHolidayLines_shouldReturnHolidays_forValidLines() throws IOException, InvalidFileFormatException {
        // Given
        var content = "2025-01-01,BRA,Ano Novo\n" +
                      "2025-12-25,BRA,Natal";
        var reader = new BufferedReader(new StringReader(content));

        // When
        var holidays = repository.processHolidayLines(reader);

        // Then
        assertEquals(2, holidays.size());
        assertEquals(LocalDate.of(2025, 1, 1), holidays.get(0).date());
        assertEquals("BRA", holidays.get(0).location());
        assertEquals("Ano Novo", holidays.get(0).description());
        assertEquals(LocalDate.of(2025, 12, 25), holidays.get(1).date());
        assertEquals("BRA", holidays.get(1).location());
        assertEquals("Natal", holidays.get(1).description());
    }

    @Test
    void processHolidayLines_shouldSkipEmptyAndCommentLines() throws IOException, InvalidFileFormatException {
        // Given
        var content = "2025-01-01,BRA,Ano Novo\n\n# This is a comment\n2025-12-25,BRA,Natal";
        var reader = new BufferedReader(new StringReader(content));

        // When
        var holidays = repository.processHolidayLines(reader);

        // Then
        assertEquals(2, holidays.size());
    }

    @Test
    void findAll_shouldReturnHolidays_forValidFile() throws HolidayLoadException {
        // Given
        var validCsv = "Date,Location,Description\n2025-01-01,BRA,Ano Novo\n2025-12-25,BRA,Natal";
        repository.setCsvContent(validCsv);

        // When
        var holidays = repository.findAll();

        // Then
        assertEquals(2, holidays.size());
        assertEquals(LocalDate.of(2025, 1, 1), holidays.get(0).date());
        assertEquals("BRA", holidays.get(0).location());
        assertEquals("Ano Novo", holidays.get(0).description());
        assertEquals(LocalDate.of(2025, 12, 25), holidays.get(1).date());
        assertEquals("BRA", holidays.get(1).location());
        assertEquals("Natal", holidays.get(1).description());
    }

    @Test
    void findAll_shouldThrowException_forEmptyFile() {
        // Given
        repository.setCsvContent("");

        // When/Then
        var exception = assertThrows(HolidayLoadException.class, () -> repository.findAll());
        assertTrue(exception.getMessage().contains("Invalid holiday file format"));
    }

    @Test
    void findAll_shouldThrowException_forInvalidHeader() {
        // Given
        var invalidCsv = "Invalid,Header\n" +
                         "2025-01-01,BRA,Ano Novo";
        repository.setCsvContent(invalidCsv);

        // When/Then
        var exception = assertThrows(HolidayLoadException.class, () -> repository.findAll());
        assertTrue(exception.getMessage().contains("Invalid holiday file format"));
    }

    @Test
    void findAll_shouldThrowException_forInvalidData() {
        // Given
        var invalidCsv = "Date,Location,Description\n" +
                         "invalid-date,BRA,Ano Novo";
        repository.setCsvContent(invalidCsv);

        // When/Then
        var exception = assertThrows(HolidayLoadException.class, () -> repository.findAll());
        assertTrue(exception.getMessage().contains("Invalid holiday file format"));
    }

    @Test
    void findAll_shouldThrowException_forFileNotFound() {
        // Given
        repository.setCsvContent(null);

        // When/Then
        var exception = assertThrows(HolidayLoadException.class, () -> repository.findAll());
        assertTrue(exception.getMessage().contains("Failed to read holidays file"));
    }
}
