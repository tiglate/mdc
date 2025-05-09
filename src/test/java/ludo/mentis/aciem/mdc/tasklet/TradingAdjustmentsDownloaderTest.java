package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.model.HttpMethod;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class TradingAdjustmentsDownloaderTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FileDownloadService fileDownloadService;

    @Mock
    private StepContribution stepContribution;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChunkContext chunkContext;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepContext stepContext;

    private TradingAdjustmentsDownloader tasklet;
    private ExecutionContext executionContext;
    private LocalDate referenceDate;
    private String fileUrl;
    private byte[] fileContent;

    @BeforeEach
    void setUp() throws Exception {
        // Set up test data
        referenceDate = LocalDate.of(2023, 5, 15);
        fileUrl = "https://example.com/tradingAdjustments";
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
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any(Map.class))).thenReturn(resource);

        // Create tasklet
        tasklet = new TradingAdjustmentsDownloader(fileDownloadService, referenceDate, fileUrl);
    }

    @Test
    void execute_shouldDownloadFileAndPopulateJobContext() throws Exception {
        // Given
        var expectedFormattedDate = referenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        // When
        var result = tasklet.execute(stepContribution, chunkContext);

        // Then
        assertEquals(RepeatStatus.FINISHED, result);
        
        // Capture the parameters passed to downloadFile
        ArgumentCaptor<Map<String, String>> parametersCaptor = ArgumentCaptor.forClass(Map.class);
        verify(fileDownloadService).downloadFile(eq(new URL(fileUrl)), eq(HttpMethod.POST), parametersCaptor.capture());
        
        // Verify parameters
        Map<String, String> capturedParameters = parametersCaptor.getValue();
        assertEquals(expectedFormattedDate, capturedParameters.get("dData1"));

        // Verify execution context was populated
        assertArrayEquals(fileContent, (byte[]) executionContext.get("fileContent"));
        assertEquals(referenceDate, executionContext.get("referenceDate"));
    }

    @Test
    void constructor_shouldUseCurrentDate_whenReferenceDateIsNull() {
        // Given
        var today = LocalDate.now();

        // When
        tasklet = new TradingAdjustmentsDownloader(fileDownloadService, null, fileUrl);

        // Then
        // We can't directly test the private field, but we can verify the behavior
        // by checking the date format in the execute method
        try {
            tasklet.execute(stepContribution, chunkContext);
            
            // Capture the parameters passed to downloadFile
            ArgumentCaptor<Map<String, String>> parametersCaptor = ArgumentCaptor.forClass(Map.class);
            verify(fileDownloadService).downloadFile(any(URL.class), eq(HttpMethod.POST), parametersCaptor.capture());
            
            // Verify parameters
            Map<String, String> capturedParameters = parametersCaptor.getValue();
            var expectedFormattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            assertEquals(expectedFormattedDate, capturedParameters.get("dData1"));
            
            // Verify reference date in execution context
            assertEquals(today, executionContext.get("referenceDate"));
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void execute_shouldThrowException_whenDownloadFails() throws Exception {
        // Given
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any(Map.class)))
                .thenThrow(new DownloadException("Download failed"));

        // When/Then
        assertThrows(DownloadException.class, () -> tasklet.execute(stepContribution, chunkContext));
    }

    @Test
    void execute_shouldThrowException_whenEmptyResponse() throws Exception {
        // Given
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any(Map.class)))
                .thenReturn(null);

        assertThrows(IllegalStateException.class, () -> tasklet.execute(stepContribution, chunkContext));
    }
}