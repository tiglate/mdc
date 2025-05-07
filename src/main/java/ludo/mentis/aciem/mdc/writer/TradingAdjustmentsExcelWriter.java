package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.TradingAdjustment;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class TradingAdjustmentsExcelWriter extends BaseExcelItemWriter<TradingAdjustment> {
    private static final String SHEET_NAME = "TradingAdjustments";
    private static final String TABLE_NAME = "Tb_TradingAdjustments";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "TradingAdjustments.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Mercadoria", "Vencimento", "Preço de ajuste anterior", "Preço de ajuste Atual",
            "Variação", "Valor do ajuste por contrato (R$)"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public TradingAdjustmentsExcelWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Ajustes do Pregão - Audit Information", referenceDate);
    }

    @Override
    public void write(Chunk<? extends TradingAdjustment> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(TradingAdjustment item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getCommodity());
        excelHelper.setCellValue(row, 1, item.getMaturity());
        excelHelper.setCellValue(row, 2, item.getPreviousAdjustmentPrice());
        excelHelper.setCellValue(row, 3, item.getCurrentAdjustmentPrice());
        excelHelper.setCellValue(row, 4, item.getVariation());
        excelHelper.setCellValue(row, 5, item.getAdjustmentValuePerContract());
    }
}