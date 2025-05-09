package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.springframework.batch.item.ExecutionContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BrazilianBondPricesDownloader extends BaseDownloaderTasklet {

    private static final String FILE_PREFIX = "ms";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    private final String baseUrl;

    public BrazilianBondPricesDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
                                         String baseUrl) {
        super(fileDownloadService, referenceDate);
        this.baseUrl = baseUrl;
    }

    @Override
    protected URL getFileUrl() throws MalformedURLException {
        String fileName = getFileName();
        return new URL(this.baseUrl + fileName);
    }

    @Override
    protected String getFileNullErrorMessage() {
        String fileName = getFileName();
        return "Downloaded file '%s' is null".formatted(fileName);
    }

    @Override
    protected String getFileEmptyErrorMessage() {
        String fileName = getFileName();
        return "Downloaded file '%s' is empty".formatted(fileName);
    }

    @Override
    protected void putAdditionalDataInContext(ExecutionContext jobContext, byte[] fileContent) {
        super.putAdditionalDataInContext(jobContext, fileContent);
        String fileName = getFileName();
        jobContext.put("fileName", fileName);
    }

    private String getFileName() {
        assert this.referenceDate != null;
        return FILE_PREFIX + this.referenceDate.format(FILE_DATE_FORMATTER) + ".txt";
    }
}
