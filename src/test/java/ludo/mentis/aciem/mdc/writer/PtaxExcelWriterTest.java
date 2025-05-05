package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.Ptax;
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
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PtaxExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private PtaxExcelWriter writer;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new PtaxExcelWriter(backupService, excelHelper, tempDir.toString());
        outputPath = tempDir.resolve("Ptax.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Then
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRow() {
        // Given
        var ptax = createSamplePtax();
        
        // When
        writer.writeRow(ptax);
        
        // Then
        // Verify interactions with excelHelper
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(new BigDecimal("5.70450")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq(new BigDecimal("5.70510")));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq(LocalDateTime.of(2025, 4, 1, 13, 7, 29, 553000000)));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSamplePtax(),
            createAnotherSamplePtax(),
            createSamplePtax()
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
        var ptax = new Ptax();
        ptax.setBuyValue(new BigDecimal("5.70450"));
        // Other fields are null
        
        // When
        writer.writeRow(ptax);
        
        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(new BigDecimal("5.70450")));
    }

    private Ptax createSamplePtax() {
        var ptax = new Ptax();
        ptax.setBuyValue(new BigDecimal("5.70450"));
        ptax.setSellValue(new BigDecimal("5.70510"));
        ptax.setTimestamp(LocalDateTime.of(2025, 4, 1, 13, 7, 29, 553000000));
        return ptax;
    }

    private Ptax createAnotherSamplePtax() {
        var ptax = new Ptax();
        ptax.setBuyValue(new BigDecimal("5.69180"));
        ptax.setSellValue(new BigDecimal("5.69230"));
        ptax.setTimestamp(LocalDateTime.of(2025, 4, 2, 13, 2, 29, 942000000));
        return ptax;
    }
}