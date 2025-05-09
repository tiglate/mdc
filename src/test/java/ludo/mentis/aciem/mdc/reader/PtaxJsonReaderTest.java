package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.Ptax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PtaxJsonReaderTest {

    private PtaxJsonReader reader;

    @BeforeEach
    void setUp() throws IOException {
        // Load the test file
        var goodFile = new ClassPathResource("Ptax_good.json");
        var goodFileContent = Files.readAllBytes(goodFile.getFile().toPath());
        reader = new PtaxJsonReader(goodFileContent);
        reader.open(new ExecutionContext());
    }

    @Test
    void shouldReadValidFile() {
        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals(new BigDecimal("5.70450"), firstItem.getBuyValue());
        assertEquals(new BigDecimal("5.70510"), firstItem.getSellValue());
        assertEquals(
            LocalDateTime.of(2025, 4, 1, 13, 7, 29, 553000000),
            firstItem.getTimestamp()
        );
    }

    @Test
    void shouldReadAllItemsFromValidFile() {
        // When/Then
        int count = 0;
        Ptax item;
        while ((item = reader.read()) != null) {
            count++;
            assertNotNull(item.getBuyValue());
            assertNotNull(item.getSellValue());
            assertNotNull(item.getTimestamp());
        }

        // The good file has 22 items
        assertEquals(22, count);
    }

    @Test
    void shouldReadSecondItemCorrectly() {
        // Skip first item
        reader.read();
        
        // Read the second item
        var secondItem = reader.read();
        
        // Then
        assertNotNull(secondItem);
        assertEquals(new BigDecimal("5.69180"), secondItem.getBuyValue());
        assertEquals(new BigDecimal("5.69230"), secondItem.getSellValue());
        assertEquals(
            LocalDateTime.of(2025, 4, 2, 13, 2, 29, 942000000),
            secondItem.getTimestamp()
        );
    }

    @Test
    void shouldThrowExceptionForNullFileContent() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new PtaxJsonReader(null));
    }
}