package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.TradingAdjustment;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Row;
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

class TradingAdjustmentsExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private TradingAdjustmentsExcelWriter writer;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var referenceDate = LocalDate.of(2023, 5, 15);
        writer = new TradingAdjustmentsExcelWriter(backupService, excelHelper, referenceDate, tempDir.toString());
        outputPath = tempDir.resolve("TradingAdjustments.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Then
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRow() {
        // Given
        var tradingAdjustment = createSampleTradingAdjustment();

        // When
        writer.writeRow(tradingAdjustment);

        // Then
        // Verify interactions with excelHelper
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq("DOL"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("JUN/23"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq(new BigDecimal("5000.00")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq(new BigDecimal("5100.00")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(new BigDecimal("100.00")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(5), eq(new BigDecimal("500.00")));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSampleTradingAdjustment(),
            createSampleTradingAdjustment(),
            createSampleTradingAdjustment()
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
        var tradingAdjustment = new TradingAdjustment();
        tradingAdjustment.setCommodity("DOL");
        // All other fields are null

        // When
        writer.writeRow(tradingAdjustment);

        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(Row.class), eq(0), eq("DOL"));
    }

    private TradingAdjustment createSampleTradingAdjustment() {
        return new TradingAdjustment(
            "DOL",
            "JUN/23",
            new BigDecimal("5000.00"),
            new BigDecimal("5100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("500.00")
        );
    }
}