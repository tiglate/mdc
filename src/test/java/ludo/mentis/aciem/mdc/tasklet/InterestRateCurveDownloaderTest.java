package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.model.HttpMethod;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

@ExtendWith(MockitoExtension.class)
class InterestRateCurveDownloaderTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private FileDownloadService fileDownloadService;

    @Mock
    private StepContribution stepContribution;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ChunkContext chunkContext;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private StepContext stepContext;

    @Captor
    private ArgumentCaptor<Map<String, String>> parametersCaptor;

    private InterestRateCurveDownloader tasklet;
    private ExecutionContext executionContext;
    private LocalDate referenceDate;
    private String fileUrl;
    private byte[] fileContent;

    @BeforeEach
    void setUp() throws Exception {
        // Set up test data
        referenceDate = LocalDate.of(2023, 5, 15);
        fileUrl = "https://example.com/api";
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
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any())).thenReturn(resource);

        // Create tasklet
        tasklet = new InterestRateCurveDownloader(fileDownloadService, referenceDate, fileUrl);
    }

    @Test
    void execute_shouldDownloadFileAndPopulateJobContext() throws Exception {
        // When
        var result = tasklet.execute(stepContribution, chunkContext);

        // Then
        assertEquals(RepeatStatus.FINISHED, result);
        verify(fileDownloadService).downloadFile(eq(new URL(fileUrl)), eq(HttpMethod.POST), parametersCaptor.capture());
        
        // Verify parameters
        Map<String, String> parameters = parametersCaptor.getValue();
        assertEquals(referenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), parameters.get("Dt_Ref"));
        assertEquals("US", parameters.get("Idioma"));
        assertEquals("csv", parameters.get("saida"));

        // Verify execution context was populated
        assertArrayEquals(fileContent, (byte[]) executionContext.get("fileContent"));
        assertEquals(referenceDate, executionContext.get("referenceDate"));
    }

    @Test
    void execute_shouldThrowException_whenDownloadFails() throws Exception {
        // Given
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any()))
                .thenThrow(new DownloadException("Download failed"));

        // When/Then
        assertThrows(DownloadException.class, () -> tasklet.execute(stepContribution, chunkContext));
    }

    @Test
    void execute_shouldUseCorrectDateFormat() throws Exception {
        // Given
        var testDate = LocalDate.of(2022, 12, 31);
        tasklet = new InterestRateCurveDownloader(fileDownloadService, testDate, fileUrl);

        // When
        tasklet.execute(stepContribution, chunkContext);

        // Then
        verify(fileDownloadService).downloadFile(eq(new URL(fileUrl)), eq(HttpMethod.POST), parametersCaptor.capture());
        
        // Verify date format in parameters
        Map<String, String> parameters = parametersCaptor.getValue();
        assertEquals("31/12/2022", parameters.get("Dt_Ref"));
    }

    @Test
    void constructor_shouldUseProvidedReferenceDate() throws Exception {
        // Given
        var customDate = LocalDate.of(2021, 3, 25);
        
        // When
        tasklet = new InterestRateCurveDownloader(fileDownloadService, customDate, fileUrl);
        tasklet.execute(stepContribution, chunkContext);
        
        // Then
        verify(fileDownloadService).downloadFile(eq(new URL(fileUrl)), eq(HttpMethod.POST), parametersCaptor.capture());
        
        Map<String, String> parameters = parametersCaptor.getValue();
        assertEquals("25/03/2021", parameters.get("Dt_Ref"));
        assertEquals(customDate, executionContext.get("referenceDate"));
    }

    @Test
    void constructor_shouldUseProvidedFileUrl() throws Exception {
        // Given
        var customUrl = "https://custom.example.com/api";
        
        // When
        tasklet = new InterestRateCurveDownloader(fileDownloadService, referenceDate, customUrl);
        tasklet.execute(stepContribution, chunkContext);
        
        // Then
        verify(fileDownloadService).downloadFile(eq(new URL(customUrl)), eq(HttpMethod.POST), any());
    }
}