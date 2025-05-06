package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialIndicatorDownloaderTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FileDownloadService fileDownloadService;

    @Mock
    private StepContribution stepContribution;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChunkContext chunkContext;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepContext stepContext;

    private FinancialIndicatorDownloader tasklet;
    private ExecutionContext executionContext;
    private String serviceUrl;
    private byte[] fileContent;

    @BeforeEach
    void setUp() throws Exception {
        // Set up test data
        serviceUrl = "https://example.com/financial-indicators";
        fileContent = "Test file content".getBytes();

        // Set up the execution context
        executionContext = new ExecutionContext();

        // Set up mocks
        var stepExecution = MetaDataInstanceFactory.createStepExecution();
        var jobExecution = stepExecution.getJobExecution();
        jobExecution.setExecutionContext(executionContext);

        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);
        when(stepContribution.getStepExecution()).thenReturn(stepExecution);

        // Create resource mock
        Resource resource = new ByteArrayResource(fileContent);
        when(fileDownloadService.downloadFile(any(URL.class))).thenReturn(resource);

        // Create tasklet
        tasklet = new FinancialIndicatorDownloader(fileDownloadService, serviceUrl);
    }

    @Test
    void execute_shouldDownloadFileAndPopulateJobContext() throws Exception {
        // When
        var result = tasklet.execute(stepContribution, chunkContext);

        // Then
        assertEquals(RepeatStatus.FINISHED, result);
        verify(fileDownloadService).downloadFile(new URL(serviceUrl));

        // Verify execution context was populated
        assertArrayEquals(fileContent, (byte[]) executionContext.get("fileContent"));
    }

    @Test
    void execute_shouldThrowException_whenDownloadFails() throws Exception {
        // Given
        when(fileDownloadService.downloadFile(any(URL.class)))
                .thenThrow(new DownloadException("Download failed"));

        // When/Then
        assertThrows(DownloadException.class, () -> tasklet.execute(stepContribution, chunkContext));
    }

    @Test
    void execute_shouldUseCorrectUrl() throws Exception {
        // Given
        var customUrl = "https://custom.example.com/api/data";
        tasklet = new FinancialIndicatorDownloader(fileDownloadService, customUrl);

        // When
        tasklet.execute(stepContribution, chunkContext);

        // Then
        verify(fileDownloadService).downloadFile(new URL(customUrl));
    }
}
