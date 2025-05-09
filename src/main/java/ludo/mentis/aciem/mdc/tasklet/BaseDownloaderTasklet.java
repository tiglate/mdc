package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.model.HttpMethod;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.core.io.Resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Base abstract class for all downloader tasklets.
 * This class provides common functionality for downloading files and handling the response.
 */
public abstract class BaseDownloaderTasklet implements Tasklet {

    protected final FileDownloadService fileDownloadService;
    protected final LocalDate referenceDate;

    /**
     * Constructor for tasklets that require a reference date.
     *
     * @param fileDownloadService The service used to download files
     * @param referenceDate The reference date for the download, defaults to the current date if null
     */
    protected BaseDownloaderTasklet(FileDownloadService fileDownloadService, LocalDate referenceDate) {
        this.fileDownloadService = fileDownloadService;
        this.referenceDate = referenceDate != null ? referenceDate : LocalDate.now();
    }

    /**
     * Constructor for tasklets that don't require a reference date.
     *
     * @param fileDownloadService The service used to download files
     */
    protected BaseDownloaderTasklet(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
        this.referenceDate = null;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) 
            throws Exception {
        var jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        URL fileUrl = getFileUrl();
        Resource fileResource;

        if (usePostMethod()) {
            fileResource = fileDownloadService.downloadFile(fileUrl, HttpMethod.POST, getRequestParameters());
        } else {
            fileResource = fileDownloadService.downloadFile(fileUrl);
        }

        if (fileResource == null) {
            throw new IllegalStateException(getFileNullErrorMessage());
        }

        var fileContent = fileResource.getContentAsByteArray();
        if (fileContent.length == 0) {
            throw new IllegalStateException(getFileEmptyErrorMessage());
        }

        // Put common data in a job context
        jobContext.put("fileContent", fileContent);

        // Put optional data in a job context
        putAdditionalDataInContext(jobContext, fileContent);

        return RepeatStatus.FINISHED;
    }

    /**
     * Get the URL for the file to download.
     *
     * @return The URL for the file
     * @throws MalformedURLException If an error occurs creating the URL
     */
    protected abstract URL getFileUrl() throws MalformedURLException;

    /**
     * Determine if this tasklet should use the POST method for downloading.
     *
     * @return true if POST method should be used, false for GET method
     */
    protected boolean usePostMethod() {
        return false;
    }

    /**
     * Get parameters for the POST request. Only used if usePostMethod() returns true.
     *
     * @return Map of request parameters
     */
    protected Map<String, String> getRequestParameters() {
        return new HashMap<>();
    }

    /**
     * Get an error message for null file.
     *
     * @return Error message
     */
    protected String getFileNullErrorMessage() {
        return "Downloaded file is null";
    }

    /**
     * Get an error message for empty file.
     *
     * @return Error message
     */
    protected String getFileEmptyErrorMessage() {
        return "Downloaded file is empty";
    }

    /**
     * Put additional data in the job context.
     * Default implementation adds referenceDate if it's not null.
     *
     * @param jobContext The job context
     * @param fileContent The downloaded file content
     */
    protected void putAdditionalDataInContext(org.springframework.batch.item.ExecutionContext jobContext, byte[] fileContent) {
        if (referenceDate != null) {
            jobContext.put("referenceDate", referenceDate);
        }
    }
}
