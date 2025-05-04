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
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrazilianBondPricesDownloaderTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FileDownloadService fileDownloadService;

    @Mock
    private StepContribution stepContribution;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChunkContext chunkContext;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepContext stepContext;

    private BrazilianBondPricesDownloader tasklet;
    private ExecutionContext executionContext;
    private LocalDate referenceDate;
    private String baseUrl;
    private byte[] fileContent;

    @BeforeEach
    void setUp() throws Exception {
        // Set up test data
        referenceDate = LocalDate.of(2023, 5, 15);
        baseUrl = "https://example.com/";
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
        tasklet = new BrazilianBondPricesDownloader(fileDownloadService, referenceDate, baseUrl);
    }

    @Test
    void execute_shouldDownloadFileAndPopulateJobContext() throws Exception {
        // Given
        var expectedFileName = "ms" + referenceDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + ".txt";
        var expectedUrl = baseUrl + expectedFileName;

        // When
        var result = tasklet.execute(stepContribution, chunkContext);

        // Then
        assertEquals(RepeatStatus.FINISHED, result);
        verify(fileDownloadService).downloadFile(new URL(expectedUrl));

        // Verify execution context was populated
        assertEquals(expectedFileName, executionContext.getString("fileName"));
        assertArrayEquals(fileContent, (byte[]) executionContext.get("fileContent"));
        assertEquals(referenceDate, executionContext.get("referenceDate"));
    }

    @Test
    void constructor_shouldUseCurrentDate_whenReferenceDateIsNull() {
        // Given
        var today = LocalDate.now();

        // When
        tasklet = new BrazilianBondPricesDownloader(fileDownloadService, null, baseUrl);

        // Then
        // We can't directly test the private field, but we can verify the behavior
        // by checking the file name format in the execute method
        try {
            tasklet.execute(stepContribution, chunkContext);
            var fileName = executionContext.getString("fileName");
            var expectedPrefix = "ms" + today.format(DateTimeFormatter.ofPattern("yyMMdd"));
            assertTrue(fileName.startsWith(expectedPrefix), 
                       "File name should start with " + expectedPrefix + " but was " + fileName);
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
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
    void execute_shouldUseCorrectUrlFormat() throws Exception {
        // Given
        // Different reference date to test URL formatting
        var testDate = LocalDate.of(2022, 12, 31);
        tasklet = new BrazilianBondPricesDownloader(fileDownloadService, testDate, baseUrl);

        var expectedFileName = "ms" + testDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + ".txt";
        var expectedUrl = baseUrl + expectedFileName;

        // When
        tasklet.execute(stepContribution, chunkContext);

        // Then
        verify(fileDownloadService).downloadFile(new URL(expectedUrl));
    }

    @Test
    void execute_shouldHandleBaseUrlWithTrailingSlash() throws Exception {
        // Given
        var baseUrlWithSlash = "https://example.com/path/";
        tasklet = new BrazilianBondPricesDownloader(fileDownloadService, referenceDate, baseUrlWithSlash);

        var expectedFileName = "ms" + referenceDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + ".txt";
        var expectedUrl = baseUrlWithSlash + expectedFileName;

        // When
        tasklet.execute(stepContribution, chunkContext);

        // Then
        verify(fileDownloadService).downloadFile(new URL(expectedUrl));
    }
}
