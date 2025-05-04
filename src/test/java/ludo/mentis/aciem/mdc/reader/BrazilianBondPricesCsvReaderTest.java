package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BrazilianBondPricesCsvReaderTest {

    private byte[] goodFileContent;
    private byte[] badFileContent;
    private String fileName;

    @BeforeEach
    void setUp() throws IOException {
        // Load test files
        var goodFile = new ClassPathResource("BrazilianBondPrices_Good.txt");
        var badFile = new ClassPathResource("BrazilianBondPrices_Bad.txt");

        goodFileContent = Files.readAllBytes(goodFile.getFile().toPath());
        badFileContent = Files.readAllBytes(badFile.getFile().toPath());
        fileName = "test.txt";
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // Given
        var reader = new BrazilianBondPricesCsvReader(goodFileContent, fileName);
        reader.open(new ExecutionContext());

        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals("LTN", firstItem.getTitle());
        assertEquals(LocalDate.of(2025, 4, 2), firstItem.getReferenceDate());
        assertEquals("100000", firstItem.getSelicCode());
        assertEquals(LocalDate.of(2022, 1, 7), firstItem.getBaseDate());
        assertEquals(LocalDate.of(2025, 7, 1), firstItem.getMaturityDate());
        assertEquals(new BigDecimal("14.4135"), firstItem.getBuyRate());
        assertEquals(new BigDecimal("14.3062"), firstItem.getSellRate());
        assertEquals(new BigDecimal("14.3685"), firstItem.getIndicativeRate());
        assertEquals(new BigDecimal("968.539902"), firstItem.getPrice());
        assertEquals(new BigDecimal("0.04678601964241"), firstItem.getStandardDeviation());
        assertEquals(new BigDecimal("14.2154"), firstItem.getLowerIntervalD0());
        assertEquals(new BigDecimal("14.5007"), firstItem.getUpperIntervalD0());
        assertEquals(new BigDecimal("14.288"), firstItem.getLowerIntervalD1());
        assertEquals(new BigDecimal("14.5722"), firstItem.getUpperIntervalD1());
        assertEquals("Calculado", firstItem.getCriteria());
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // Given
        var reader = new BrazilianBondPricesCsvReader(goodFileContent, fileName);
        reader.open(new ExecutionContext());

        // When/Then
        int count = 0;
        BrazilianBondPrice item;
        while ((item = reader.read()) != null) {
            count++;
            assertNotNull(item.getTitle());
            assertNotNull(item.getReferenceDate());
            assertNotNull(item.getSelicCode());
            assertNotNull(item.getBaseDate());
            assertNotNull(item.getMaturityDate());
            // Some numeric fields might be null if they have "--" in the file
            // So we don't assert notNull for all fields
        }

        // The good file has 48 data lines (52 total lines - 3 header lines - 1 empty line at the end)
        assertEquals(48, count);
    }

    @Test
    void shouldHandleSpecialCases() throws Exception {
        // Given
        var reader = new BrazilianBondPricesCsvReader(goodFileContent, fileName);
        reader.open(new ExecutionContext());

        // When - Skip to line 32 which has "--" values
        BrazilianBondPrice item = null;
        for (int i = 0; i < 29; i++) { // 0-based index, so 29 reads to get to the 30th item (line 32)
            item = reader.read();
        }

        // Then
        assertNotNull(item);
        assertEquals("LFT", item.getTitle());
        assertEquals(LocalDate.of(2031, 6, 1), item.getMaturityDate());
        // The fields with "--" should be null
        assertNull(item.getLowerIntervalD0());
        assertNull(item.getUpperIntervalD0());
        assertNotNull(item.getLowerIntervalD1()); // This field has a value
    }

    @Test
    void shouldThrowExceptionForIncompleteLine() {
        // Given
        var reader = new BrazilianBondPricesCsvReader(badFileContent, fileName);
        reader.open(new ExecutionContext());

        // When/Then
        // Read until we hit the incomplete line (line 26)
        for (int i = 0; i < 22; i++) {
            assertDoesNotThrow(reader::read);
        }

        // The 23rd read should throw an exception (line 26 in the file, after skipping 3 header lines)
        assertThrows(FlatFileParseException.class, reader::read);
    }

    @Test
    void shouldHandleDuplicateFields() {
        // Given
        var reader = new BrazilianBondPricesCsvReader(badFileContent, fileName);
        reader.open(new ExecutionContext());

        // When/Then
        // Skip the incomplete line by catching the exception
        for (int i = 0; i < 23; i++) {
            try {
                reader.read();
            } catch (Exception e) {
                // Ignore the exception for the incomplete line
            }
        }

        // Try to read the line with duplicate fields (line 31)
        // This might throw an exception depending on how the reader handles extra fields
        assertThrows(Exception.class, () -> {
            for (int i = 0; i < 5; i++) {
                reader.read();
            }
        });
    }

    @Test
    void shouldThrowExceptionForNullFileContent() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new BrazilianBondPricesCsvReader(null, fileName));
    }

    @Test
    void shouldThrowExceptionForNullFileName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new BrazilianBondPricesCsvReader(goodFileContent, null));
    }

    @Test
    void shouldThrowExceptionForEmptyFileName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new BrazilianBondPricesCsvReader(goodFileContent, ""));
    }

    @Test
    void shouldThrowExceptionForNegativeLinesToSkip() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new BrazilianBondPricesCsvReader(goodFileContent, fileName, -1));
    }

    @Test
    void shouldWorkWithCustomLinesToSkip() throws Exception {
        // Given - Skip 4 lines instead of the default 3
        var reader = new BrazilianBondPricesCsvReader(goodFileContent, fileName, 4);
        reader.open(new ExecutionContext());

        // When
        var firstItem = reader.read();

        // Then - The first item should be the second item in the file (since we skipped one more line)
        assertNotNull(firstItem);
        assertEquals("LTN", firstItem.getTitle());
        assertEquals(LocalDate.of(2025, 4, 2), firstItem.getReferenceDate());
        assertEquals("100000", firstItem.getSelicCode());
        assertEquals(LocalDate.of(2023, 7, 7), firstItem.getBaseDate());
        assertEquals(LocalDate.of(2025, 10, 1), firstItem.getMaturityDate());
    }
}
