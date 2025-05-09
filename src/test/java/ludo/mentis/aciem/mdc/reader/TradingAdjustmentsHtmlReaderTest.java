package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.TradingAdjustment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ParseException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class TradingAdjustmentsHtmlReaderTest {

    private byte[] goodFileContent;
    private byte[] badFileContent;

    @BeforeEach
    void setUp() throws IOException {
        // Load test files
        var goodFile = new ClassPathResource("TradingAdjustments_Good.html");
        var badFile = new ClassPathResource("TradingAdjustments_Bad.html");

        goodFileContent = Files.readAllBytes(goodFile.getFile().toPath());
        badFileContent = Files.readAllBytes(badFile.getFile().toPath());
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // Given
        var reader = new TradingAdjustmentsHtmlReader(goodFileContent);

        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals("ABEVO - Contrato Futuro de ABEV3", firstItem.getCommodity());
        assertEquals("K25", firstItem.getMaturity());
        assertEquals(new BigDecimal("14.18"), firstItem.getPreviousAdjustmentPrice());
        assertEquals(new BigDecimal("14.12"), firstItem.getCurrentAdjustmentPrice());
        assertEquals(new BigDecimal("-0.06"), firstItem.getVariation());
        assertEquals(new BigDecimal("0.06"), firstItem.getAdjustmentValuePerContract());
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // Given
        var reader = new TradingAdjustmentsHtmlReader(goodFileContent);

        // When/Then
        int count = 0;
        TradingAdjustment item;
        while ((item = reader.read()) != null) {
            count++;
            assertNotNull(item.getCommodity());
            assertNotNull(item.getMaturity());
            assertNotNull(item.getPreviousAdjustmentPrice());
            assertNotNull(item.getCurrentAdjustmentPrice());
            assertNotNull(item.getVariation());
            assertNotNull(item.getAdjustmentValuePerContract());
        }

        // The good file has 4 data rows
        assertEquals(590, count);
    }

    @Test
    void shouldHandleRowspanCorrectly() throws Exception {
        // Given
        var reader = new TradingAdjustmentsHtmlReader(goodFileContent);

        // When
        // Skip first 3 items
        reader.read();
        reader.read();
        reader.read();
        var fourthItem = reader.read();

        // Then
        assertNotNull(fourthItem);
        assertEquals("AFS - Rande da África do Sul (em USD)", fourthItem.getCommodity()); // Should inherit from previous row
        assertEquals("N25", fourthItem.getMaturity());
        assertEquals(new BigDecimal("18509.100"), fourthItem.getPreviousAdjustmentPrice());
        assertEquals(new BigDecimal("18363.400"), fourthItem.getCurrentAdjustmentPrice());
    }

    @Test
    void shouldHandleNullFileContent() {
        // Given
        var reader = new TradingAdjustmentsHtmlReader(null);

        // When/Then
        // Should throw NullPointerException when trying to read
        assertThrows(NullPointerException.class, reader::read);
    }

    @Test
    void shouldThrowExceptionForMissingTable() {
        // Given
        byte[] htmlWithoutTable = "<html><body>No table here</body></html>".getBytes();

        // When/Then
        var reader = new TradingAdjustmentsHtmlReader(htmlWithoutTable);
        assertThrows(IOException.class, reader::read);
    }

    @Test
    void shouldSkipRowsWithMissingColumns() throws Exception {
        // Given
        var missingColumnsFile = new ClassPathResource("TradingAdjustments_MissingColumns.html");
        var missingColumnsContent = Files.readAllBytes(missingColumnsFile.getFile().toPath());
        var reader = new TradingAdjustmentsHtmlReader(missingColumnsContent);

        // When
        // First row is valid
        var firstItem = reader.read();

        // Then
        // First row should be read correctly
        assertNotNull(firstItem);
        assertEquals("DOL", firstItem.getCommodity());
        assertEquals("JUN/23", firstItem.getMaturity());

        // Second row has missing columns and should be skipped
        // Since there are no more valid rows, read() should return null
        assertNull(reader.read());
    }

    @Test
    void shouldThrowExceptionForInvalidNumberFormat() {
        // Given
        var reader = new TradingAdjustmentsHtmlReader(badFileContent);

        // When/Then
        // Should throw ParseException because of the invalid number format
        assertThrows(ParseException.class, reader::read);
    }

}
