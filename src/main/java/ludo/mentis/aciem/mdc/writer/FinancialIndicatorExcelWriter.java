package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class FinancialIndicatorExcelWriter extends BaseExcelItemWriter<FinancialIndicator> {
    private static final String SHEET_NAME = "FI";
    private static final String TABLE_NAME = "Tb_FI";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "FinancialIndicators.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "ID", "Group", "Description", "Value", "Last Update"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public FinancialIndicatorExcelWriter(BackupService backupService, ExcelHelper excelHelper, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Financial Indicators - Audit Information", LocalDate.now());
    }

    @Override
    public void write(Chunk<? extends FinancialIndicator> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(FinancialIndicator item) {
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
}
