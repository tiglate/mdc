package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class BrazilianBondPricesExcelWriter extends BaseExcelItemWriter<BrazilianBondPrice> {
    private static final String SHEET_NAME = "Anbima";
    private static final String TABLE_NAME = "Tb_Anbima";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "BrazilianBondPrices.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Título", "Data Referência", "Código SELIC", "Data Base/Emissão",
            "Data Vencimento", "Tx. Compra", "Tx. Venda", "Tx. Indicativas", "PU",
            "Desvio Padrão", "Interv. Ind. Inf. (D0)", "Interv. Ind. Sup. (D0)",
            "Interv. Ind. Inf. (D+1)", "Interv. Ind. Sup. (D+1)", "Critério"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public BrazilianBondPricesExcelWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Brazilian Bond Prices - Audit Information", referenceDate);
    }

    @Override
    public void write(Chunk<? extends BrazilianBondPrice> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    private void writeRow(BrazilianBondPrice item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getTitle());
        excelHelper.setCellValue(row, 1, item.getReferenceDate());
        excelHelper.setCellValue(row, 2, item.getSelicCode());
        excelHelper.setCellValue(row, 3, item.getBaseDate());
        excelHelper.setCellValue(row, 4, item.getMaturityDate());
        excelHelper.setCellValue(row, 5, item.getBuyRate());
        excelHelper.setCellValue(row, 6, item.getSellRate());
        excelHelper.setCellValue(row, 7, item.getIndicativeRate());
        excelHelper.setCellValue(row, 8, item.getPrice());
        excelHelper.setCellValue(row, 9, item.getStandardDeviation());
        excelHelper.setCellValue(row, 10, item.getLowerIntervalD0());
        excelHelper.setCellValue(row, 11, item.getUpperIntervalD0());
        excelHelper.setCellValue(row, 12, item.getLowerIntervalD1());
        excelHelper.setCellValue(row, 13, item.getUpperIntervalD1());
        excelHelper.setCellValue(row, 14, item.getCriteria());
    }
}