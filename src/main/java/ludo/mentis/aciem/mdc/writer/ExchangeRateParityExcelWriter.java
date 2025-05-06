package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.ExchangeRateParity;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class ExchangeRateParityExcelWriter extends BaseExcelItemWriter<ExchangeRateParity> {
    private static final String SHEET_NAME = "ExchangeRateParity";
    private static final String TABLE_NAME = "Tb_ExchangeRateParity";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "ExchangeRateParity.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Reference Date", "Currency ID", "Type", "Currency Code",
            "Buy Rate", "Sell Rate", "Buy Parity", "Sell Parity"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public ExchangeRateParityExcelWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Exchange Rate Parity - Audit Information", referenceDate);
    }

    @Override
    public void write(Chunk<? extends ExchangeRateParity> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(ExchangeRateParity item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getReferenceDate());
        excelHelper.setCellValue(row, 1, item.getCurrencyId());
        excelHelper.setCellValue(row, 2, item.getType());
        excelHelper.setCellValue(row, 3, item.getCurrencyCode());
        excelHelper.setCellValue(row, 4, item.getBuyRate());
        excelHelper.setCellValue(row, 5, item.getSellRate());
        excelHelper.setCellValue(row, 6, item.getBuyParity());
        excelHelper.setCellValue(row, 7, item.getSellParity());
    }
}