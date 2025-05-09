package ludo.mentis.aciem.mdc.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterestRateCurveCsvReaderTest {

    private byte[] goodFileContent;
    private byte[] emptyFileContent;
    private byte[] invalidHeaderFileContent;
    private byte[] invalidLineFileContent;

    @BeforeEach
    void setUp() {
        // Prepare good file content
        String goodContent = """
			05/05/2025;ANBIMA;ETTJ;PARAMETROS;NELSON-SIEGEL-SVENSSON;PRE;REAL
			PREFIXADOS;7.96287626860661E-02;5.96513971347563E-02;8.64348297182261E-02;0.181498016176063;2.03671405327417;0.177752217216416
			IPCA;6.12345678901234E-02;4.56789012345678E-02;7.89012345678901E-02;0.123456789012345;1.98765432109876;0.123456789012345""";
        goodFileContent = goodContent.getBytes(StandardCharsets.UTF_8);

        // Prepare bad file content with invalid header
        String badHeaderContent = "05/05/2025;ANBIMA;ETTJ;PARAMETROS;NELSON-SIEGEL-SVENSSON\n" + // Missing 2 fields
                "PREFIXADOS;7.96287626860661E-02;5.96513971347563E-02;8.64348297182261E-02;0.181498016176063;2.03671405327417;0.177752217216416";
        invalidHeaderFileContent = badHeaderContent.getBytes(StandardCharsets.UTF_8);

        // Prepare bad file content with invalid line
        String badLineContent = "05/05/2025;ANBIMA;ETTJ;PARAMETROS;NELSON-SIEGEL-SVENSSON;PRE;REAL\n" +
                "PREFIXADOS;7.96287626860661E-02;5.96513971347563E-02;8.64348297182261E-02;0.181498016176063;2.03671405327417"; // Missing 1 field
        invalidLineFileContent = badLineContent.getBytes(StandardCharsets.UTF_8);

        // Prepare empty file content
        emptyFileContent = "".getBytes(StandardCharsets.UTF_8);

        // Prepare general bad file content
        String badContent = "This is not a valid CSV file";
        //noinspection ResultOfMethodCallIgnored
        badContent.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // Given
        var reader = new InterestRateCurveCsvReader(goodFileContent);

        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals(LocalDate.of(2025, 5, 5), firstItem.getReferenceDate());
        assertEquals("PREFIXADOS", firstItem.getDescription());
        assertEquals(new BigDecimal("7.96287626860661E-02"), firstItem.getBeta1());
        assertEquals(new BigDecimal("5.96513971347563E-02"), firstItem.getBeta2());
        assertEquals(new BigDecimal("8.64348297182261E-02"), firstItem.getBeta3());
        assertEquals(new BigDecimal("0.181498016176063"), firstItem.getBeta4());
        assertEquals(new BigDecimal("2.03671405327417"), firstItem.getLambda1());
        assertEquals(new BigDecimal("0.177752217216416"), firstItem.getLambda2());
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // Given
        var reader = new InterestRateCurveCsvReader(goodFileContent);

        // When
        var firstItem = reader.read();
        var secondItem = reader.read();
        var thirdItem = reader.read(); // Should be null as there are only 2 items

        // Then
        assertNotNull(firstItem);
        assertEquals("PREFIXADOS", firstItem.getDescription());
        
        assertNotNull(secondItem);
        assertEquals("IPCA", secondItem.getDescription());
        
        assertNull(thirdItem); // End of data
    }

    @Test
    void shouldThrowExceptionForEmptyFile() {
        // Given
        var reader = new InterestRateCurveCsvReader(emptyFileContent);

        // When/Then
        var exception = assertThrows(IOException.class, reader::read);
        assertEquals("Empty file", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidHeader() {
        // Given
        var reader = new InterestRateCurveCsvReader(invalidHeaderFileContent);

        // When/Then
        var exception = assertThrows(IOException.class, reader::read);
        assertTrue(exception.getMessage().startsWith("Invalid header line:"));
    }

    @Test
    void shouldThrowExceptionForInvalidLine() {
        // Given
        var reader = new InterestRateCurveCsvReader(invalidLineFileContent);

        // When/Then
        var exception = assertThrows(IOException.class, reader::read);
        assertTrue(exception.getMessage().startsWith("Invalid line:"));
    }

    @Test
    void shouldSkipEmptyLines() throws Exception {
        // Given
        String contentWithEmptyLine = """
			05/05/2025;ANBIMA;ETTJ;PARAMETROS;NELSON-SIEGEL-SVENSSON;PRE;REAL
			
			PREFIXADOS;7.96287626860661E-02;5.96513971347563E-02;8.64348297182261E-02;0.181498016176063;2.03671405327417;0.177752217216416"""; // Empty line
        var reader = new InterestRateCurveCsvReader(contentWithEmptyLine.getBytes(StandardCharsets.UTF_8));

        // When
        var item = reader.read();

        // Then
        assertNotNull(item);
        assertEquals("PREFIXADOS", item.getDescription());
    }

    @Test
    void shouldHandleMultipleReads() throws Exception {
        // Given
        var reader = new InterestRateCurveCsvReader(goodFileContent);

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
}