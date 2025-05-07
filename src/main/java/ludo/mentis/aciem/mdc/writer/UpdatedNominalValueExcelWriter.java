package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.UpdatedNominalValue;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class UpdatedNominalValueExcelWriter extends BaseExcelItemWriter<UpdatedNominalValue> {
    private static final String SHEET_NAME = "UpdatedNominalValue";
    private static final String TABLE_NAME = "Tb_UpdatedNominalValue";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "UpdatedNominalValue.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Reference Date", "Security", "SELIC Code", "VNA", "Index",
            "Reference", "Valid Since"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public UpdatedNominalValueExcelWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Interest Rate Curves - Audit Information", referenceDate);
    }

    @Override
    public void write(Chunk<? extends UpdatedNominalValue> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(UpdatedNominalValue item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getReferenceDate());
        excelHelper.setCellValue(row, 1, item.getSecurity());
        excelHelper.setCellValue(row, 2, item.getSelicCode());
        excelHelper.setCellValue(row, 3, item.getValue());
        excelHelper.setCellValue(row, 4, item.getIndex());
        excelHelper.setCellValue(row, 5, item.getReference());
        excelHelper.setCellValue(row, 6, item.getValidSince());
    }
}