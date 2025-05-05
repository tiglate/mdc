package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

import java.net.URL;

public class FinancialIndicatorDownloader implements Tasklet {

    private final FileDownloadService fileDownloadService;
    private final String serviceUrl;

    public FinancialIndicatorDownloader(FileDownloadService fileDownloadService, String serviceUrl) {
        this.fileDownloadService = fileDownloadService;
        this.serviceUrl = serviceUrl;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        var fileUrl = new URL(serviceUrl);

        var jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        jobContext.put("fileContent", fileDownloadService.downloadFile(fileUrl).getContentAsByteArray());

        return RepeatStatus.FINISHED;
    }
}
