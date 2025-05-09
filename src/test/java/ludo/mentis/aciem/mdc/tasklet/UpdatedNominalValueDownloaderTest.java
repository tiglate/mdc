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
class UpdatedNominalValueDownloaderTest {

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

    private UpdatedNominalValueDownloader tasklet;
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
        tasklet = new UpdatedNominalValueDownloader(fileDownloadService, referenceDate, fileUrl);
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
        assertEquals(referenceDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")), parameters.get("Data"));
        assertEquals("2", parameters.get("escolha"));
        assertEquals("US", parameters.get("Idioma"));
        assertEquals("csv", parameters.get("saida"));

        // Verify execution context was populated
        assertArrayEquals(fileContent, (byte[]) executionContext.get("fileContent"));
        assertEquals(referenceDate, executionContext.get("referenceDate"));
    }

    @Test
    void execute_shouldThrowException_whenEmptyResponse() throws Exception {
        // Given
        when(fileDownloadService.downloadFile(any(URL.class), eq(HttpMethod.POST), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> tasklet.execute(stepContribution, chunkContext));
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
        tasklet = new UpdatedNominalValueDownloader(fileDownloadService, testDate, fileUrl);

        // When
        tasklet.execute(stepContribution, chunkContext);

        // Then
        verify(fileDownloadService).downloadFile(eq(new URL(fileUrl)), eq(HttpMethod.POST), parametersCaptor.capture());
        
        // Verify date format in parameters
        Map<String, String> parameters = parametersCaptor.getValue();
        assertEquals("31122022", parameters.get("Data"));
    }
}