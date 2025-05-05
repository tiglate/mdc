package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseExcelItemWriterTest {

    @TempDir
    Path tempDir;

    @Mock
    private BackupService backupService;

    @Mock
    private ExcelHelper excelHelper;

    private TestExcelItemWriter writer;
    private Path outputPath;
    private LocalDate referenceDate;
    private static final String FILE_NAME = "test.xlsx";
    private static final String[] COLUMN_HEADERS = {"Column1", "Column2", "Column3"};

    @BeforeEach
    void setUp() throws IOException {
        referenceDate = LocalDate.of(2025, 5, 2);
        this.outputPath = tempDir.resolve(FILE_NAME);
        this.createEmptySpreadsheet();
        writer = new TestExcelItemWriter(backupService, excelHelper, referenceDate, tempDir.toString(), FILE_NAME);
    }

    @AfterEach
    void cleanup() throws IOException {
        this.deleteEmptySpreadsheet();
    }

    @Test
    void shouldInitializeWorkbook() throws Exception {
        // When
        try (var workbook = writer.initializeWorkbook("Sheet1")) {
            // Then
            assertNotNull(workbook);
            verify(backupService, times(1)).backup(any());
        }
    }

    @Test
    void shouldCreateAuditSheet() throws IOException {
        // Given
        try (var workbook = new XSSFWorkbook()) {
            // When
            writer.createAuditSheet(workbook, "Test Audit", referenceDate);

            // Then
            assertNotNull(workbook.getSheet("Audit"));
            verify(excelHelper, times(5)).setCellValue(any(Row.class), anyInt(), anyString());
        }
    }

    @Test
    void shouldWriteHeader() throws IOException {
        try (var workbook = new XSSFWorkbook()) {
            // Given
            var sheet = workbook.createSheet("Test");

            // When
            writer.writeHeader(sheet, COLUMN_HEADERS);

            // Then
            var headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals(COLUMN_HEADERS.length, headerRow.getLastCellNum());
        }
    }

    @Test
    void shouldAutosizeColumns() throws IOException {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Test");
            assertDoesNotThrow(() -> writer.autosizeColumns(sheet, COLUMN_HEADERS));
        }
    }

    @Test
    void shouldCreateTable() throws IOException {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Test");
            var headerRow = sheet.createRow(0);
            for (var i = 0; i < COLUMN_HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(COLUMN_HEADERS[i]);
            }
            var dataRow = sheet.createRow(1);
            for (var i = 0; i < COLUMN_HEADERS.length; i++) {
                dataRow.createCell(i).setCellValue("Data" + i);
            }
            assertDoesNotThrow(() -> writer.createTable(sheet, COLUMN_HEADERS, "TestTable", "TableStyleMedium2"));
        }
    }

    @Test
    void shouldSaveWorkbook() throws IOException {
        // Given
        try (var workbook = new XSSFWorkbook()) {
            workbook.createSheet("Test");

            // When
            writer.saveWorkbook(workbook);

            // Then
            assertTrue(Files.exists(outputPath));
        }
    }

    @Test
    void shouldHandleBackup() throws Exception {
        // When
        writer.handleBackup();

        // Then
        verify(backupService, times(1)).backup(any());
    }

    @Test
    void shouldRemoveExistingSheet() throws IOException {
        // Given
        try (var workbook = new XSSFWorkbook()) {
            var sheetName = "TestSheet";
            workbook.createSheet(sheetName);

            // When
            writer.removeExistingSheet(workbook, sheetName);

            // Then
            assertNull(workbook.getSheet(sheetName));
        }
    }

    @Test
    void shouldWriteItems() throws Exception {
        // Given
        var items = Arrays.asList("Item1", "Item2", "Item3");
        var chunk = new Chunk<>(items);

        // When
        writer.write(chunk);

        // Then
        assertTrue(Files.exists(outputPath));
        assertEquals(3, writer.getWriteCount());
    }

    // Concrete implementation of BaseExcelItemWriter for testing
    private static class TestExcelItemWriter extends BaseExcelItemWriter<String> {
        private int writeCount = 0;

        public TestExcelItemWriter(BackupService backupService, ExcelHelper excelHelper, 
                                  LocalDate referenceDate, String outputDir, String fileName) {
            super(backupService, excelHelper, referenceDate, outputDir, fileName);
        }

        @Override
        public void write(Chunk<? extends String> chunk) throws Exception {
            var workbook = this.initializeWorkbook();
            var sheet = workbook.createSheet("Test");
            this.writeHeader(sheet, new String[]{"Value"});

            for (var item : chunk) {
                writeItem(sheet, item);
                writeCount++;
            }

            this.saveWorkbook(workbook);
        }

        private void writeItem(Sheet sheet, String item) {
            var row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(item);
        }

        public int getWriteCount() {
            return writeCount;
        }
    }

    private void createEmptySpreadsheet() throws IOException {
        try (var workbook = new XSSFWorkbook()) {
            workbook.createSheet("Sheet1");
            try (var out = Files.newOutputStream(this.outputPath)) {
                workbook.write(out);
            }
        }
    }

    private void deleteEmptySpreadsheet() throws IOException {
        Files.deleteIfExists(this.outputPath);
    }
}
