package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FinancialIndicatorExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private FinancialIndicatorExcelWriter writer;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new FinancialIndicatorExcelWriter(backupService, excelHelper, tempDir.toString());
        outputPath = tempDir.resolve("FinancialIndicators.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Then
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRowWithValue() {
        // Given
        var indicator = createSampleIndicatorWithValue();
        
        // When
        writer.writeRow(indicator);
        
        // Then
        // Verify interactions with excelHelper
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(10008989L));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("TAXAS DE CÂMBIO"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq("DÓLAR CUPOM LIMPO"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq(new BigDecimal("5.6530")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(LocalDate.of(2025, 5, 2)));
    }

    @Test
    void shouldWriteRowWithRate() {
        // Given
        var indicator = createSampleIndicatorWithRate();
        
        // When
        writer.writeRow(indicator);
        
        // Then
        // Verify interactions with excelHelper
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(9800656L));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("TAXAS DE JUROS NACIONAL"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq("TAXA SELIC"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq(new BigDecimal("14.15")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(LocalDate.of(2025, 5, 2)));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSampleIndicatorWithValue(),
            createSampleIndicatorWithRate(),
            createSampleIndicatorWithValue()
        );
        var chunk = new Chunk<>(items);
        
        // When
        writer.write(chunk);
        
        // Then
        // Verify that the file was created
        assertTrue(Files.exists(outputPath));
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        var indicator = new FinancialIndicator();
        indicator.setSecurityIdentificationCode(12345L);
        indicator.setDescription("Test Indicator");
        // All other fields are null
        
        // When
        writer.writeRow(indicator);
        
        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(12345L));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq("Test Indicator"));
    }

    private FinancialIndicator createSampleIndicatorWithValue() {
        var indicator = new FinancialIndicator();
        indicator.setSecurityIdentificationCode(10008989L);
        indicator.setDescription("DÓLAR CUPOM LIMPO");
        indicator.setGroupDescription("TAXAS DE CÂMBIO");
        indicator.setValue(new BigDecimal("5.6530"));
        indicator.setRate(null);
        indicator.setLastUpdate(LocalDate.of(2025, 5, 2));
        return indicator;
    }

    private FinancialIndicator createSampleIndicatorWithRate() {
        var indicator = new FinancialIndicator();
        indicator.setSecurityIdentificationCode(9800656L);
        indicator.setDescription("TAXA SELIC");
        indicator.setGroupDescription("TAXAS DE JUROS NACIONAL");
        indicator.setValue(null);
        indicator.setRate(new BigDecimal("14.15"));
        indicator.setLastUpdate(LocalDate.of(2025, 5, 2));
        return indicator;
    }
}