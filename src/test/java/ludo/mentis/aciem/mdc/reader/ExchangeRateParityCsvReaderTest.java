package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.ExchangeRateParity;
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

class ExchangeRateParityCsvReaderTest {

    private byte[] goodFileContent;
    private byte[] badFileContent;
    private String fileName;

    @BeforeEach
    void setUp() throws IOException {
        // Load test files
        var goodFile = new ClassPathResource("ExchangeRateParity_Good.csv");
        var badFile = new ClassPathResource("ExchangeRateParity_Bad.csv");

        goodFileContent = Files.readAllBytes(goodFile.getFile().toPath());
        badFileContent = Files.readAllBytes(badFile.getFile().toPath());
        fileName = "test.csv";
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // Given
        var reader = new ExchangeRateParityCsvReader(goodFileContent, fileName);
        reader.open(new ExecutionContext());

        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals(LocalDate.of(2025, 5, 5), firstItem.getReferenceDate());
        assertEquals("005", firstItem.getCurrencyId());
        assertEquals("A", firstItem.getType());
        assertEquals("AFN", firstItem.getCurrencyCode());
        assertEquals(new BigDecimal("0.07966000"), firstItem.getBuyRate());
        assertEquals(new BigDecimal("0.07990000"), firstItem.getSellRate());
        assertEquals(new BigDecimal("70.74000000"), firstItem.getBuyParity());
        assertEquals(new BigDecimal("70.94000000"), firstItem.getSellParity());
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // Given
        var reader = new ExchangeRateParityCsvReader(goodFileContent, fileName);
        reader.open(new ExecutionContext());

        // When/Then
        int count = 0;
        ExchangeRateParity item;
        while ((item = reader.read()) != null) {
            count++;
            assertNotNull(item.getReferenceDate());
            assertNotNull(item.getCurrencyId());
            assertNotNull(item.getType());
            assertNotNull(item.getCurrencyCode());
            assertNotNull(item.getBuyRate());
            assertNotNull(item.getSellRate());
            assertNotNull(item.getBuyParity());
            assertNotNull(item.getSellParity());
        }

        // The good file has 157 data lines (excluding the empty line at the end)
        assertEquals(157, count);
    }

    @Test
    void shouldThrowExceptionForNullFileContent() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ExchangeRateParityCsvReader(null, fileName));
    }

    @Test
    void shouldThrowExceptionForNullFileName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ExchangeRateParityCsvReader(goodFileContent, null));
    }

    @Test
    void shouldThrowExceptionForEmptyFileName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ExchangeRateParityCsvReader(goodFileContent, ""));
    }

    @Test
    void shouldThrowExceptionForNegativeLinesToSkip() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ExchangeRateParityCsvReader(goodFileContent, fileName, -1));
    }

    @Test
    void shouldWorkWithCustomLinesToSkip() throws Exception {
        // Given - Skip 1 line
        var reader = new ExchangeRateParityCsvReader(goodFileContent, fileName, 1);
        reader.open(new ExecutionContext());

        // When
        var firstItem = reader.read();

        // Then - The first item should be the second item in the file (since we skipped one line)
        assertNotNull(firstItem);
        assertEquals(LocalDate.of(2025, 5, 5), firstItem.getReferenceDate());
        assertEquals("009", firstItem.getCurrencyId());
        assertEquals("A", firstItem.getType());
        assertEquals("ETB", firstItem.getCurrencyCode());
        assertEquals(new BigDecimal("0.04156000"), firstItem.getBuyRate());
        assertEquals(new BigDecimal("0.04315000"), firstItem.getSellRate());
        assertEquals(new BigDecimal("130.99750000"), firstItem.getBuyParity());
        assertEquals(new BigDecimal("135.99750000"), firstItem.getSellParity());
    }

    @Test
    void shouldThrowExceptionForIncompleteLine() throws Exception {
        // Given
        var reader = new ExchangeRateParityCsvReader(badFileContent, fileName);
        reader.open(new ExecutionContext());

        // When/Then
        // Skip the first line which is valid
        reader.read();
        // The second read should throw an exception for the incomplete line
        assertThrows(FlatFileParseException.class, reader::read);
    }
}
