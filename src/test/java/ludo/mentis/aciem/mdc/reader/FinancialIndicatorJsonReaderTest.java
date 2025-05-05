package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialIndicatorJsonReaderTest {

    private FinancialIndicatorJsonReader reader;

    @BeforeEach
    void setUp() throws IOException {
        // Load the test file
        var goodFile = new ClassPathResource("FinancialIndicator_Good.json");
        var goodFileContent = Files.readAllBytes(goodFile.getFile().toPath());
        reader = new FinancialIndicatorJsonReader(goodFileContent);
        reader.open(new ExecutionContext());
    }

    @Test
    void shouldReadValidFile() throws Exception {
        // When
        var firstItem = reader.read();

        // Then
        assertNotNull(firstItem);
        assertEquals(10014464L, firstItem.getSecurityIdentificationCode());
        assertEquals("DIF OPER CASADA - COMPRA", firstItem.getDescription());
        assertEquals("TAXAS DE CÂMBIO", firstItem.getGroupDescription());
        assertNull(firstItem.getValue()); // Value is empty in the JSON
        assertEquals(new BigDecimal("38.06"), firstItem.getRate()); // Rate is "38,06" in the JSON
        assertEquals(LocalDate.of(2025, 5, 2), firstItem.getLastUpdate()); // Date is "02/05/2025" in the JSON
    }

    @Test
    void shouldReadAllItemsFromValidFile() throws Exception {
        // When/Then
        int count = 0;
        FinancialIndicator item;
        while ((item = reader.read()) != null) {
            count++;
            assertNotNull(item.getSecurityIdentificationCode());
            assertNotNull(item.getDescription());
            assertNotNull(item.getGroupDescription());
            assertNotNull(item.getLastUpdate());
            // Either value or rate should be non-null
            assertTrue(item.getValue() != null || item.getRate() != null);
        }

        // The good file has 7 items
        assertEquals(7, count);
    }

    @Test
    void shouldHandleValueAndRateCorrectly() throws Exception {
        // The first item has rate but no value
        var item1 = reader.read();
        assertNotNull(item1);
        assertNull(item1.getValue());
        assertNotNull(item1.getRate());
        
        // The second item has value but no rate
        var item2 = reader.read();
        assertNotNull(item2);
        assertNotNull(item2.getValue());
        assertNull(item2.getRate());
    }

    @Test
    void shouldThrowExceptionForNullFileContent() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new FinancialIndicatorJsonReader(null));
    }
}