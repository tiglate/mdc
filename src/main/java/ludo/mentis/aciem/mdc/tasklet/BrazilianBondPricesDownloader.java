package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BrazilianBondPricesDownloader implements Tasklet {

    private static final String FILE_PREFIX = "ms";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    private final String baseUrl;
    private final LocalDate referenceDate;
    private final FileDownloadService fileDownloadService;

    public BrazilianBondPricesDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
                                         String baseUrl) {
        this.fileDownloadService = fileDownloadService;
        this.referenceDate = referenceDate != null ? referenceDate : LocalDate.now();
        this.baseUrl = baseUrl;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext)
            throws Exception {
        var fileName = FILE_PREFIX + this.referenceDate.format(FILE_DATE_FORMATTER) + ".txt";
        var fileUrl = new URL(this.baseUrl + fileName);

        var jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        jobContext.put("fileName", fileName);
        jobContext.put("fileContent", fileDownloadService.downloadFile(fileUrl).getContentAsByteArray());
        jobContext.put("referenceDate", this.referenceDate);

        return RepeatStatus.FINISHED;
    }
}
