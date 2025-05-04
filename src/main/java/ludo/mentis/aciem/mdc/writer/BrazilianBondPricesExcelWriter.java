package ludo.mentis.aciem.mdc.writer;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
    private int currentRow = 1; // 0 is for header

    public BrazilianBondPricesExcelWriter(LocalDate referenceDate, String outputDir) {
        var date = referenceDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.outputPath = Paths.get(outputDir, "anbima-" + date + ".xlsx");

        try {
            if (Files.exists(outputPath)) {
                this.workbook = WorkbookFactory.create(Files.newInputStream(outputPath));
                this.sheet = workbook.getSheet("Anbima");
            } else {
                this.workbook = new XSSFWorkbook();
                this.sheet = workbook.createSheet("Anbima");
                writeHeader();
            }
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
    }

    @Override
    public void write(Chunk<? extends BrazilianBondPrice> chunk) throws Exception {
        for (var item : chunk) {
            var row = sheet.createRow(currentRow++);
            row.createCell(0).setCellValue(item.getTitle());
            row.createCell(1).setCellValue(item.getReferenceDate().toString());
            row.createCell(2).setCellValue(item.getSelicCode());
            row.createCell(3).setCellValue(item.getBaseDate().toString());
            row.createCell(4).setCellValue(item.getMaturityDate().toString());
            row.createCell(5).setCellValue(item.getBuyRate().doubleValue());
            row.createCell(6).setCellValue(item.getSellRate().doubleValue());
            row.createCell(7).setCellValue(item.getIndicativeRate().doubleValue());
            row.createCell(8).setCellValue(item.getPrice().doubleValue());
            row.createCell(9).setCellValue(item.getStandardDeviation().doubleValue());
            row.createCell(10).setCellValue(item.getLowerIntervalD0().doubleValue());
            row.createCell(11).setCellValue(item.getUpperIntervalD0().doubleValue());
            row.createCell(12).setCellValue(item.getLowerIntervalD1().doubleValue());
            row.createCell(13).setCellValue(item.getUpperIntervalD1().doubleValue());
            row.createCell(14).setCellValue(item.getCriteria());
        }

        try (var out = Files.newOutputStream(outputPath)) {
            workbook.write(out);
        }
    }
}
