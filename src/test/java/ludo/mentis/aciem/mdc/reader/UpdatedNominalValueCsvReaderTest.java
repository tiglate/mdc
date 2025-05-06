package ludo.mentis.aciem.mdc.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdatedNominalValueCsvReaderTest {

    private byte[] goodFileContent;
    private byte[] emptyFileContent;
    private byte[] invalidHeaderFileContent;
    private byte[] invalidLineFileContent;
    private byte[] missingReferenceDateFileContent;

    @BeforeEach
    void setUp() {
        // Prepare good file content
        String goodContent = """
			Reference Date :  05/06/2025
			Titulo;SELIC Code;UNV;Index;Reference;Valid Since
			NTN-B;760199;4499.253373;0.42;P;04/28/2025
			LTN;100000;1000.000000;1.00;P;05/01/2025""";
        goodFileContent = goodContent.getBytes(StandardCharsets.UTF_8);

        // Prepare bad file content with invalid header
        String badHeaderContent = """
			Reference Date :  05/06/2025
			Titulo;SELIC Code;UNV;Index;Reference
			NTN-B;760199;4499.253373;0.42;P;04/28/2025"""; // Missing 1 field
        invalidHeaderFileContent = badHeaderContent.getBytes(StandardCharsets.UTF_8);

        // Prepare bad file content with invalid line
        String badLineContent = """
			Reference Date :  05/06/2025
			Titulo;SELIC Code;UNV;Index;Reference;Valid Since
			NTN-B;760199;4499.253373;0.42;P"""; // Missing 1 field
        invalidLineFileContent = badLineContent.getBytes(StandardCharsets.UTF_8);

        // Prepare file content with missing reference date
        String missingRefDateContent = "Titulo;SELIC Code;UNV;Index;Reference;Valid Since\n" +
                "NTN-B;760199;4499.253373;0.42;P;04/28/2025";
        missingReferenceDateFileContent = missingRefDateContent.getBytes(StandardCharsets.UTF_8);

        // Prepare empty file content
        emptyFileContent = "".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // Given
        var reader = new UpdatedNominalValueCsvReader(goodFileContent);

        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals(LocalDate.of(2025, 5, 6), firstItem.getReferenceDate());
        assertEquals("NTN-B", firstItem.getSecurity());
        assertEquals("760199", firstItem.getSelicCode());
        assertEquals(new BigDecimal("4499.253373"), firstItem.getValue());
        assertEquals(new BigDecimal("0.42"), firstItem.getIndex());
        assertEquals("P", firstItem.getReference());
        assertEquals(LocalDate.of(2025, 4, 28), firstItem.getValidSince());
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // Given
        var reader = new UpdatedNominalValueCsvReader(goodFileContent);

        // When
        var firstItem = reader.read();
        var secondItem = reader.read();
        var thirdItem = reader.read(); // Should be null as there are only 2 items

        // Then
        assertNotNull(firstItem);
        assertEquals("NTN-B", firstItem.getSecurity());
        
        assertNotNull(secondItem);
        assertEquals("LTN", secondItem.getSecurity());
        
        assertNull(thirdItem); // End of data
    }

    @Test
    void shouldHandleEmptyFile() throws Exception {
        // Given
        var reader = new UpdatedNominalValueCsvReader(emptyFileContent);

        // When
        var item = reader.read();

        // Then
        assertNull(item); // Empty file should return null
    }

    @Test
    void shouldThrowExceptionForInvalidHeader() {
        // Given
        var reader = new UpdatedNominalValueCsvReader(invalidHeaderFileContent);

        // When/Then
        var exception = assertThrows(IOException.class, reader::read);
        assertTrue(exception.getMessage().startsWith("Invalid header line:"));
    }

    @Test
    void shouldThrowExceptionForInvalidLine() {
        // Given
        var reader = new UpdatedNominalValueCsvReader(invalidLineFileContent);

        // When/Then
        var exception = assertThrows(IOException.class, reader::read);
        assertTrue(exception.getMessage().startsWith("Invalid line:"));
    }

    @Test
    void shouldHandleFileWithMissingReferenceDate() throws Exception {
        // Given
        var reader = new UpdatedNominalValueCsvReader(missingReferenceDateFileContent);

        // When
        var item = reader.read();

        // Then
        assertNotNull(item);
        assertEquals(LocalDate.now(), item.getReferenceDate()); // Should default to today
        assertEquals("NTN-B", item.getSecurity());
    }

    @Test
    void shouldSkipEmptyLines() throws Exception {
        // Given
        String contentWithEmptyLine = """
			Reference Date :  05/06/2025
			Titulo;SELIC Code;UNV;Index;Reference;Valid Since
			
			NTN-B;760199;4499.253373;0.42;P;04/28/2025"""; // Empty line
        var reader = new UpdatedNominalValueCsvReader(contentWithEmptyLine.getBytes(StandardCharsets.UTF_8));

        // When
        var item = reader.read();

        // Then
        assertNotNull(item);
        assertEquals("NTN-B", item.getSecurity());
    }

    @Test
    void shouldHandleMultipleReads() throws Exception {
        // Given
        var reader = new UpdatedNominalValueCsvReader(goodFileContent);

        // When - Read all items
        var items = 0;
        while (reader.read() != null) {
            items++;
        }

        // Then
        assertEquals(2, items);

        // When - Try to read again after reaching the end
        var item = reader.read();

        // Then
        assertNull(item);
    }

    @Test
    void shouldHandleCommasInNumericValues() throws Exception {
        // Given
        String contentWithCommas = """
			Reference Date :  05/06/2025
			Titulo;SELIC Code;UNV;Index;Reference;Valid Since
			NTN-B;760199;4,499.253373;10,421.12;P;04/28/2025""";
        var reader = new UpdatedNominalValueCsvReader(contentWithCommas.getBytes(StandardCharsets.UTF_8));

        // When
        var item = reader.read();

        // Then
        assertNotNull(item);
        assertEquals(new BigDecimal("4499.253373"), item.getValue());
        assertEquals(new BigDecimal("10421.12"), item.getIndex());
    }

    @Test
    void shouldThrowExceptionForInvalidDateFormat() {
        // Given
        String contentWithInvalidDate = """
			Reference Date :  2025-05-06
			Titulo;SELIC Code;UNV;Index;Reference;Valid Since
			NTN-B;760199;4499.253373;0.42;P;04/28/2025""";// Invalid date format
        var reader = new UpdatedNominalValueCsvReader(contentWithInvalidDate.getBytes(StandardCharsets.UTF_8));

        // When/Then
        var exception = assertThrows(Exception.class, reader::read);
        assertTrue(exception.getMessage().contains("could not be parsed"));
    }
}