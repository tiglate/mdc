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

public class UpdatedNominalValueDownloader implements Tasklet {

	private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");
	
    private final String fileUrl;
    private final LocalDate referenceDate;
    private final FileDownloadService fileDownloadService;
    
	public UpdatedNominalValueDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
			String fileUrl) {
		this.fileUrl = fileUrl;
		this.referenceDate = referenceDate != null? referenceDate : LocalDate.now();
		this.fileDownloadService = fileDownloadService;
	}

	@Override
	public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        var jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        
        var parameters = new HashMap<String, String>();
        parameters.put("Data", this.referenceDate.format(FILE_DATE_FORMATTER));
        parameters.put("escolha", "2");
        parameters.put("Idioma", "US");
        parameters.put("saida", "csv");

        var fileResource = fileDownloadService.downloadFile(new URL(this.fileUrl), HttpMethod.POST, parameters);
        if (fileResource == null) {
            throw new IllegalStateException("Downloaded file is null");
        }
        var fileContent = fileResource.getContentAsByteArray();
        if (fileContent.length == 0) {
            throw new IllegalStateException("Downloaded file is empty");
        }

        jobContext.put("fileContent", fileContent);
        jobContext.put("referenceDate", this.referenceDate);

        return RepeatStatus.FINISHED;
	}

}
