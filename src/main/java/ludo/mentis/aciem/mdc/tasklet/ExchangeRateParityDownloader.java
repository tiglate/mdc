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

public class ExchangeRateParityDownloader implements Tasklet {

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String baseUrl;
    private final LocalDate referenceDate;
    private final FileDownloadService fileDownloadService;

    public ExchangeRateParityDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
                                         String baseUrl) {
        this.fileDownloadService = fileDownloadService;
        this.referenceDate = referenceDate != null ? referenceDate : LocalDate.now();
        this.baseUrl = baseUrl;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext)
            throws Exception {
        var fileName = this.referenceDate.format(FILE_DATE_FORMATTER) + ".csv";
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
