package ludo.mentis.aciem.mdc.writer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import ludo.mentis.aciem.mdc.model.InterestRateCurve;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;

@ExtendWith(MockitoExtension.class)
class InterestRateCurveExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private InterestRateCurveExcelWriter writer;
    private Path outputPath;
    private LocalDate referenceDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        referenceDate = LocalDate.of(2023, 5, 15);
        writer = new InterestRateCurveExcelWriter(backupService, excelHelper, referenceDate, tempDir.toString());
        outputPath = tempDir.resolve("InterestRateCurve.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Verify that excelHelper.init was called
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRow() {
        // Given
        var item = createSampleInterestRateCurve();
        
        // When
        writer.writeRow(item);
        
        // Then
        // Verify cell values were set
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(item.getReferenceDate()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq(item.getDescription()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq(item.getBeta1()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq(item.getBeta2()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(item.getBeta3()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(5), eq(item.getBeta4()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(6), eq(item.getLambda1()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(7), eq(item.getLambda2()));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSampleInterestRateCurve(),
            createSampleInterestRateCurve(),
            createSampleInterestRateCurve()
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
        var item = new InterestRateCurve();
        item.setReferenceDate(referenceDate);
        item.setDescription("Test Curve");
        // All other fields are null
        
        // When
        writer.writeRow(item);
        
        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(referenceDate));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("Test Curve"));
        // No need to verify null values explicitly - just ensure no exceptions are thrown
    }

    private InterestRateCurve createSampleInterestRateCurve() {
        var curve = new InterestRateCurve();
        curve.setReferenceDate(referenceDate);
        curve.setDescription("PRE");
        curve.setBeta1(new BigDecimal("0.05"));
        curve.setBeta2(new BigDecimal("0.03"));
        curve.setBeta3(new BigDecimal("0.02"));
        curve.setBeta4(new BigDecimal("0.01"));
        curve.setLambda1(new BigDecimal("0.7"));
        curve.setLambda2(new BigDecimal("0.4"));
        return curve;
    }
}