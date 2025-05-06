package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.InterestRateCurve;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

public class InterestRateCurveExcelWriter extends BaseExcelItemWriter<InterestRateCurve> {
    private static final String SHEET_NAME = "InterestRateCurve";
    private static final String TABLE_NAME = "Tb_InterestRateCurve";
    private static final String TABLE_STYLE = "TableStyleMedium2";
    private static final String FILE_NAME = "InterestRateCurve.xlsx";
    private static final String[] COLUMN_HEADERS = {
            "Reference Date", "Description", "Beta 1", "Beta 2",
            "Beta 3", "Beta 4", "Lambda 1", "Lambda 2"
    };

    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;

    public InterestRateCurveExcelWriter(BackupService backupService, ExcelHelper excelHelper, LocalDate referenceDate, String outputDir) {
        super(backupService, excelHelper, LocalDate.now(), outputDir, FILE_NAME);
        this.workbook = this.initializeWorkbook(SHEET_NAME);
        this.sheet = workbook.createSheet(SHEET_NAME);
        this.excelHelper.init(this.sheet);
        this.writeHeader(this.sheet, COLUMN_HEADERS);
        this.createAuditSheet(this.workbook, "Interest Rate Curves - Audit Information", referenceDate);
    }

    @Override
    public void write(Chunk<? extends InterestRateCurve> chunk) throws Exception {
        for (var item : chunk) {
            this.writeRow(item);
        }
        this.autosizeColumns(sheet, COLUMN_HEADERS);
        this.createTable(sheet, COLUMN_HEADERS, TABLE_NAME, TABLE_STYLE);
        this.saveWorkbook(this.workbook);
    }

    protected void writeRow(InterestRateCurve item) {
        var row = sheet.createRow(currentRow++);
        excelHelper.setCellValue(row, 0, item.getReferenceDate());
        excelHelper.setCellValue(row, 1, item.getDescription());
        excelHelper.setCellValue(row, 2, item.getBeta1());
        excelHelper.setCellValue(row, 3, item.getBeta2());
        excelHelper.setCellValue(row, 4, item.getBeta3());
        excelHelper.setCellValue(row, 5, item.getBeta4());
        excelHelper.setCellValue(row, 6, item.getLambda1());
        excelHelper.setCellValue(row, 7, item.getLambda2());
    }
}