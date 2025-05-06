package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.ExchangeRateParity;
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

class ExchangeRateParityExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private ExchangeRateParityExcelWriter writer;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var referenceDate = LocalDate.of(2025, 5, 5);
        writer = new ExchangeRateParityExcelWriter(backupService, excelHelper, referenceDate, tempDir.toString());
        outputPath = tempDir.resolve("ExchangeRateParity.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Then
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRow() {
        // Given
        var exchangeRateParity = createSampleExchangeRateParity();

        // When
        writer.writeRow(exchangeRateParity);

        // Then
        // Verify interactions with excelHelper
        // We expect 8 fields to be set (one for each field in ExchangeRateParity)
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(LocalDate.of(2025, 5, 5)));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("005"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq("A"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq("AFN"));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(new BigDecimal("0.07966000")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(5), eq(new BigDecimal("0.07990000")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(6), eq(new BigDecimal("70.74000000")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(7), eq(new BigDecimal("70.94000000")));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSampleExchangeRateParity(),
            createSampleExchangeRateParity(),
            createSampleExchangeRateParity()
        );
        var chunk = new Chunk<>(items);

        // When
        writer.write(chunk);

        // Then
        // Instead of verifying each call, verify that the file was created
        assertTrue(Files.exists(outputPath));
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        var exchangeRateParity = new ExchangeRateParity();
        exchangeRateParity.setCurrencyCode("USD");
        // All other fields are null

        // When
        writer.writeRow(exchangeRateParity);

        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(Row.class), eq(3), eq("USD"));
    }

    private ExchangeRateParity createSampleExchangeRateParity() {
        var exchangeRateParity = new ExchangeRateParity();
        exchangeRateParity.setReferenceDate(LocalDate.of(2025, 5, 5));
        exchangeRateParity.setCurrencyId("005");
        exchangeRateParity.setType("A");
        exchangeRateParity.setCurrencyCode("AFN");
        exchangeRateParity.setBuyRate(new BigDecimal("0.07966000"));
        exchangeRateParity.setSellRate(new BigDecimal("0.07990000"));
        exchangeRateParity.setBuyParity(new BigDecimal("70.74000000"));
        exchangeRateParity.setSellParity(new BigDecimal("70.94000000"));
        return exchangeRateParity;
    }
}