package ludo.mentis.aciem.mdc.tasklet;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import ludo.mentis.aciem.mdc.model.HttpMethod;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.springframework.lang.NonNull;

public class TradingAdjustmentsDownloader implements Tasklet {

	private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
    private final String fileUrl;
    private final LocalDate referenceDate;
    private final FileDownloadService fileDownloadService;
    
	public TradingAdjustmentsDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
			String fileUrl) {
		this.fileUrl = fileUrl;
		this.referenceDate = referenceDate != null ? referenceDate : LocalDate.now();
		this.fileDownloadService = fileDownloadService;
	}

	@Override
	public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        var jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        
        var parameters = new HashMap<String, String>();
        parameters.put("dData1", this.referenceDate.format(FILE_DATE_FORMATTER));

        var file = fileDownloadService.downloadFile(new URL(this.fileUrl), HttpMethod.POST, parameters);

        jobContext.put("fileContent", file != null ? file.getContentAsByteArray() : new byte[] {});
        jobContext.put("referenceDate", this.referenceDate);

        return RepeatStatus.FINISHED;
	}

}
