package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.Ptax;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class PtaxExcelWriter extends BaseExcelItemWriter<Ptax> {
    private static final String SHEET_NAME = "PX";
    private static final String TABLE_NAME = "Tb_Ptax";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "Ptax.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Buy Value", "Sell Value", "Last Update"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public PtaxExcelWriter(BackupService backupService, ExcelHelper excelHelper, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "PTAX", LocalDate.now());
    }

    @Override
    public void write(Chunk<? extends Ptax> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(Ptax item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getBuyValue());
        excelHelper.setCellValue(row, 1, item.getSellValue());
        excelHelper.setCellValue(row, 2, item.getTimestamp());
    }
}
