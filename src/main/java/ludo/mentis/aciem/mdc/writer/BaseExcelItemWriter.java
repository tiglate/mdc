package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.exception.BackupException;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class BaseExcelItemWriter<T> implements ItemWriter<T> {
    private static final String AUDIT_SHEET_NAME = "Audit";
    protected final BackupService backupService;
    protected final Path outputPath;
    protected final LocalDate referenceDate;
    protected final ExcelHelper excelHelper;

    protected BaseExcelItemWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate,
                                  String outputDir, String fileName) {
        this.backupService = backupService;
        this.excelHelper = excelHelper;
        this.outputPath = Path.of(outputDir, fileName);
        this.referenceDate = referenceDate;
    }

    protected Workbook initializeWorkbook() {
        return this.initializeWorkbook(null);
    }

    protected Workbook initializeWorkbook(String sheetName) {
        this.handleBackup();
        try {
            if (Files.exists(outputPath)) {
                var workbook = WorkbookFactory.create(Files.newInputStream(outputPath));
                if (sheetName != null && workbook.getSheet(sheetName) != null) {
                    removeExistingSheet(workbook, sheetName);
                }
                return workbook;
            }
            return new XSSFWorkbook();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void handleBackup() {
        if (outputPath.toFile().exists()) {
            try {
                backupService.backup(outputPath.toString());
            } catch (Exception ex) {
                throw new BackupException("Could not backup existing file: " + outputPath, ex);
            }
        }
    }

    protected void removeExistingSheet(Workbook workbook, String sheetName) {
        var index = workbook.getSheetIndex(sheetName);
        if (index >= 0) {
            workbook.removeSheetAt(index);
        }
    }

    protected void createAuditSheet(Workbook workbook, String title, LocalDate referenceDate) {
        var auditSheet = workbook.createSheet(AUDIT_SHEET_NAME);

        // Create rows for audit information
        var titleRow = auditSheet.createRow(0);
        this.excelHelper.setCellValue(titleRow, 0, title);
        auditSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // Reference date
        var dateRow = auditSheet.createRow(2);
        this.excelHelper.setCellValue(dateRow, 0, "Reference Date:");
        this.excelHelper.setCellValue(dateRow, 1, referenceDate);

        // Creation timestamp
        var timestampRow = auditSheet.createRow(3);
        this.excelHelper.setCellValue(timestampRow, 0, "Created At:");
        this.excelHelper.setCellValue(timestampRow, 1, LocalDateTime.now());

        // User information
        var userRow = auditSheet.createRow(4);
        this.excelHelper.setCellValue(userRow, 0, "Created By:");
        this.excelHelper.setCellValue(userRow, 1, System.getProperty("user.name"));

        // Auto-size columns
        auditSheet.autoSizeColumn(0);
        auditSheet.autoSizeColumn(1);
    }

    protected void createTable(Sheet sheet, String[] columnHeaders, String tableName, String tableStyle) {
        var area = new AreaReference(
                new CellReference(0, 0),
                new CellReference(sheet.getLastRowNum(), columnHeaders.length - 1),
                SpreadsheetVersion.EXCEL2007
        );
        var xssfSheet = (XSSFSheet) sheet;
        var table = xssfSheet.createTable(area);
        table.setName(tableName);
        table.setStyleName(tableStyle);
    }

    protected void writeHeader(Sheet sheet, String[] columnHeaders) {
        var header = sheet.createRow(0);
        for (var i = 0; i < columnHeaders.length; i++) {
            header.createCell(i).setCellValue(columnHeaders[i]);
        }
    }

    protected void saveWorkbook(Workbook workbook) throws IOException {
    	if (!Files.exists(this.outputPath.getParent())) {
            Files.createDirectories(this.outputPath.getParent());
        }
        try (var out = Files.newOutputStream(this.outputPath)) {
            workbook.write(out);
        }
        workbook.close();
    }

    protected void autosizeColumns(Sheet sheet, String[] columnHeaders) {
        for (var i = 0; i < columnHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
