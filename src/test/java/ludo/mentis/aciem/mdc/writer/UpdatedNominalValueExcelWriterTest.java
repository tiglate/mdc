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

import ludo.mentis.aciem.mdc.model.UpdatedNominalValue;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;

@ExtendWith(MockitoExtension.class)
class UpdatedNominalValueExcelWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private UpdatedNominalValueExcelWriter writer;
    private Path outputPath;
    private LocalDate referenceDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        referenceDate = LocalDate.of(2023, 5, 15);
        writer = new UpdatedNominalValueExcelWriter(backupService, excelHelper, referenceDate, tempDir.toString());
        outputPath = tempDir.resolve("UpdatedNominalValue.xlsx");
    }

    @Test
    void shouldInitializeCorrectly() {
        // Verify that excelHelper.init was called
        verify(excelHelper, times(1)).init(any(Sheet.class));
    }

    @Test
    void shouldWriteRow() {
        // Given
        var updatedNominalValue = createSampleUpdatedNominalValue();
        
        // When
        writer.writeRow(updatedNominalValue);
        
        // Then
        // Verify cell values were set
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(updatedNominalValue.getReferenceDate()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq(updatedNominalValue.getSecurity()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(2), eq(updatedNominalValue.getSelicCode()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(3), eq(updatedNominalValue.getValue()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(4), eq(updatedNominalValue.getIndex()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(5), eq(updatedNominalValue.getReference()));
        verify(excelHelper, times(1)).setCellValue(any(), eq(6), eq(updatedNominalValue.getValidSince()));
    }

    @Test
    void shouldWriteChunk() throws Exception {
        // Given
        var items = Arrays.asList(
            createSampleUpdatedNominalValue(),
            createSampleUpdatedNominalValue(),
            createSampleUpdatedNominalValue()
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
        var updatedNominalValue = new UpdatedNominalValue();
        updatedNominalValue.setReferenceDate(referenceDate);
        updatedNominalValue.setSecurity("NTN-B");
        // All other fields are null
        
        // When
        writer.writeRow(updatedNominalValue);
        
        // Then
        // Should not throw any exceptions
        verify(excelHelper, times(1)).setCellValue(any(), eq(0), eq(referenceDate));
        verify(excelHelper, times(1)).setCellValue(any(), eq(1), eq("NTN-B"));
        // No need to verify null values explicitly - just ensure no exceptions are thrown
    }

    private UpdatedNominalValue createSampleUpdatedNominalValue() {
        var value = new UpdatedNominalValue();
        value.setReferenceDate(referenceDate);
        value.setSecurity("NTN-B");
        value.setSelicCode("760199");
        value.setValue(new BigDecimal("3721.951234"));
        value.setIndex(new BigDecimal("1.0567"));
        value.setReference("IPCA");
        value.setValidSince(LocalDate.of(2023, 5, 1));
        return value;
    }
}