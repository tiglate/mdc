package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import ludo.mentis.aciem.mdc.service.BackupService;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class FinancialIndicatorExcelWriter implements ItemWriter<FinancialIndicator> {
    private static final String SHEET_NAME = "FI";
    private static final String AUDIT_SHEET_NAME = "Audit";
    private static final String TABLE_NAME = "Tb_FI";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "FinancialIndicators.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Security ID Code", "Group", "Description", "Value", "Last Update"
    };

    private final Path outputPath;
    private final Workbook workbook;
    private final Sheet sheet;
    private final ExcelHelper excelHelper;
    private int currentRow = 1;
    private final Logger log = LoggerFactory.getLogger(FinancialIndicatorExcelWriter.class);

    public FinancialIndicatorExcelWriter(BackupService backupService, String outputDir) {
        this.outputPath = Paths.get(outputDir, FILE_NAME);
        this.workbook = initializeWorkbook(backupService);
        this.sheet = createSheet();
        this.excelHelper = new ExcelHelper(sheet);
        writeHeader();
        createAuditSheet();
    }

    private void createAuditSheet() {
        Sheet auditSheet = workbook.createSheet(AUDIT_SHEET_NAME);

        // Create rows for audit information
        Row titleRow = auditSheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Financial Indicators - Audit Information");

        // Creation timestamp
        Row timestampRow = auditSheet.createRow(2);
        Cell timestampLabelCell = timestampRow.createCell(0);
        timestampLabelCell.setCellValue("Created At:");

        // Create a timestamp cell with date formatting
        Cell timestampValueCell = timestampRow.createCell(1);
        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        timestampValueCell.setCellValue(now);
        timestampValueCell.setCellStyle(excelHelper.getDateCellStyle());

        // User information
        Row userRow = auditSheet.createRow(3);
        Cell userLabelCell = userRow.createCell(0);
        userLabelCell.setCellValue("Created By:");
        Cell userValueCell = userRow.createCell(1);
        userValueCell.setCellValue(System.getProperty("user.name"));

        // Auto-size columns
        auditSheet.autoSizeColumn(0);
        auditSheet.autoSizeColumn(1);
    }

    private Workbook initializeWorkbook(BackupService backupService) {
        handleBackup(backupService);
        try {
            if (Files.exists(outputPath)) {
                var wb = WorkbookFactory.create(Files.newInputStream(outputPath));
                removeExistingSheet(wb);
                return wb;
            }
            return new XSSFWorkbook();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void handleBackup(BackupService backupService) {
        if (outputPath.toFile().exists()) {
            try {
                backupService.backup(outputPath.toString());
            } catch (Exception ex) {
                log.error("Could not backup existing file: {}", outputPath, ex);
            }
        }
    }

    private void removeExistingSheet(Workbook workbook) {
        var index = workbook.getSheetIndex(SHEET_NAME);
        if (index >= 0) {
            workbook.removeSheetAt(index);
        }
    }

    private Sheet createSheet() {
        return workbook.createSheet(SHEET_NAME);
    }

    private void writeHeader() {
        var header = sheet.createRow(0);
        for (var i = 0; i < COLUMN_HEADERS.length; i++) {
            header.createCell(i).setCellValue(COLUMN_HEADERS[i]);
        }
    }

    @Override
    public void write(Chunk<? extends FinancialIndicator> chunk) throws Exception {
        for (var item : chunk) {
            writeRow(item);
        }
        finalizeSheet();
        saveWorkbook();
    }

    private void writeRow(FinancialIndicator item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getSecurityIdentificationCode());
        excelHelper.setCellValue(row, 1, item.getGroupDescription());
        excelHelper.setCellValue(row, 2, item.getDescription());
        if (item.getValue() != null) {
            excelHelper.setCellValue(row, 3, item.getValue());
        } else if (item.getRate() != null) {
            excelHelper.setCellValue(row, 3, item.getRate());
        }
        excelHelper.setCellValue(row, 4, item.getLastUpdate());
    }

    private void finalizeSheet() {
        for (var i = 0; i < COLUMN_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
        createTable();
    }

    private void createTable() {
        var area = new AreaReference(
                new CellReference(0, 0),
                new CellReference(sheet.getLastRowNum(), COLUMN_HEADERS.length - 1),
                SpreadsheetVersion.EXCEL2007
        );
        var xssfSheet = (XSSFSheet) sheet;
        var table = xssfSheet.createTable(area);
        table.setName(TABLE_NAME);
        table.setStyleName(TABLE_STYLE);
    }

    private void saveWorkbook() throws IOException {
        try (var out = Files.newOutputStream(outputPath)) {
            workbook.write(out);
        }
        workbook.close();
    }

    private static class ExcelHelper {
        private final Sheet sheet;
        private final CellStyle dateCellStyle;

        ExcelHelper(Sheet sheet) {
            this.sheet = sheet;
            this.dateCellStyle = createDateCellStyle(sheet.getWorkbook());
        }

        private CellStyle createDateCellStyle(Workbook workbook) {
            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            return cellStyle;
        }

        CellStyle getDateCellStyle() {
            return dateCellStyle;
        }

        void setCellValue(Row row, int column, String value) {
            if (value != null) {
                row.createCell(column).setCellValue(value);
            }
        }

        void setCellValue(Row row, int column, LocalDate value) {
            if (value != null) {
                Cell cell = row.createCell(column);
                cell.setCellValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                cell.setCellStyle(dateCellStyle);
            }
        }

        void setCellValue(Row row, int column, Number value) {
            if (value != null) {
                row.createCell(column).setCellValue(value.doubleValue());
            }
        }
    }
}
