package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import ludo.mentis.aciem.mdc.service.BackupService;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BrazilianBondPricesExcelWriter implements ItemWriter<BrazilianBondPrice> {
    private final Path outputPath;
    private final Workbook workbook;
    private final Sheet sheet;
    private int currentRow = 1;
    private int headersCount;
    private final BackupService backupService;

    public BrazilianBondPricesExcelWriter(BackupService backupService, LocalDate referenceDate, String outputDir) {
        this.backupService = backupService;
        //var date = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.outputPath = Paths.get(outputDir, "BrazilianBondPrices.xlsx");

        if (this.outputPath.toFile().exists()) {
            try {
                this.backupService.backup(this.outputPath.toString());
            }
            catch (Exception ex) {
                throw new RuntimeException("Could not backup existing file: " + this.outputPath, ex);
            }
        }

        try {
            if (Files.exists(outputPath)) {
                this.workbook = WorkbookFactory.create(Files.newInputStream(outputPath));
                var index = workbook.getSheetIndex("Anbima");
                if (index >= 0) {
                    workbook.removeSheetAt(index);
                }
            } else {
                this.workbook = new XSSFWorkbook();
            }
            this.sheet = workbook.createSheet("Anbima");
            writeHeader();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeHeader() {
        var headers = new String[]{ "Titulo", "Data Referencia", "Codigo SELIC", "Data Base/Emissao",
                "Data Vencimento", "Tx. Compra", "Tx. Venda", "Tx. Indicativas", "PU", "Desvio padrao",
                "Interv. Ind. Inf. (D0)", "Interv. Ind. Sup. (D0)", "Interv. Ind. Inf. (D+1)", "Interv. Ind. Sup. (D+1)", "Criterio"};
        var header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
        headersCount = headers.length;
    }

    @Override
    public void write(Chunk<? extends BrazilianBondPrice> chunk) throws Exception {
        for (var item : chunk) {
            var row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue(item.getTitle());
            if (item.getReferenceDate() != null) {
                row.createCell(1).setCellValue(item.getReferenceDate().toString());
            }
            row.createCell(2).setCellValue(item.getSelicCode());
            if (item.getBaseDate() != null) {
                row.createCell(3).setCellValue(item.getBaseDate().toString());
            }
            if (item.getMaturityDate() != null) {
                row.createCell(4).setCellValue(item.getMaturityDate().toString());
            }
            if (item.getBuyRate() != null) {
                row.createCell(5).setCellValue(item.getBuyRate().doubleValue());
            }
            if (item.getSellRate() != null) {
                row.createCell(6).setCellValue(item.getSellRate().doubleValue());
            }
            if (item.getIndicativeRate() != null) {
                row.createCell(7).setCellValue(item.getIndicativeRate().doubleValue());
            }
            if (item.getPrice() != null) {
                row.createCell(8).setCellValue(item.getPrice().doubleValue());
            }
            if (item.getStandardDeviation() != null) {
                row.createCell(9).setCellValue(item.getStandardDeviation().doubleValue());
            }
            if (item.getLowerIntervalD0() != null) {
                row.createCell(10).setCellValue(item.getLowerIntervalD0().doubleValue());
            }
            if (item.getUpperIntervalD0() != null) {
                row.createCell(11).setCellValue(item.getUpperIntervalD0().doubleValue());
            }
            if (item.getLowerIntervalD1() != null) {
                row.createCell(12).setCellValue(item.getLowerIntervalD1().doubleValue());
            }
            if (item.getUpperIntervalD1() != null) {
                row.createCell(13).setCellValue(item.getUpperIntervalD1().doubleValue());
            }
            row.createCell(14).setCellValue(item.getCriteria());
        }

        // Auto-size columns
        for (int i = 0; i < headersCount; i++) {
            sheet.autoSizeColumn(i);
        }

        // Create table
        var area = new AreaReference(
                new CellReference(0, 0),
                new CellReference(sheet.getLastRowNum(), headersCount - 1),
                SpreadsheetVersion.EXCEL2007
        );
        var table = ((XSSFSheet) sheet).createTable(area);
        table.setName("Tb_Anbima");
        table.setStyleName("TableStyleMedium2");

        try (var out = Files.newOutputStream(this.outputPath)) {
            workbook.write(out);
        }

        workbook.close();
    }
}
