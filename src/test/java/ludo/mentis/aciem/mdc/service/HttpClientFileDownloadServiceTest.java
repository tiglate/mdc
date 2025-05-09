package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.config.HttpClientProperties;
import ludo.mentis.aciem.mdc.exception.DownloadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpClientFileDownloadServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpClientProperties properties;

    @Mock
    private HttpResponse<byte[]> byteArrayResponse;

    @Mock
    private HttpResponse<Path> fileResponse;

    private HttpClientFileDownloadService service;

    @TempDir
    Path tempDir;

    private URL testUrl;
    private Path testDestinationPath;
    private byte[] testResponseBody;

    @BeforeEach
    void setUp() throws Exception {
        service = new HttpClientFileDownloadService(httpClient, properties);

        // Set up test data
        testUrl = new URL("https://example.com/test.txt");
        testDestinationPath = tempDir.resolve("test.txt");
        testResponseBody = "Test response content".getBytes();
    }

    @Test
    void downloadFile_shouldReturnResource_whenHttpClientReturnsSuccessfulResponse() throws Exception {
        // Given
        when(properties.getRequestTimeoutMinutes()).thenReturn(5);
        when(byteArrayResponse.statusCode()).thenReturn(200);
        when(byteArrayResponse.body()).thenReturn(testResponseBody);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofByteArray())))
                .thenReturn(byteArrayResponse);

        // When
        var result = service.downloadFile(testUrl);

        // Then
        assertNotNull(result);
        assertEquals(testResponseBody.length, result.contentLength());
        verify(httpClient).send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofByteArray()));
    }

    @Test
    void downloadFile_shouldThrowDownloadException_whenResponseBodyIsNull() throws Exception {
        // Given
        when(properties.getRequestTimeoutMinutes()).thenReturn(5);
        when(byteArrayResponse.statusCode()).thenReturn(200);
        when(byteArrayResponse.body()).thenReturn(null);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofByteArray())))
                .thenReturn(byteArrayResponse);

        // When/Then
        assertThrows(
            DownloadException.class, 
            () -> service.downloadFile(testUrl),
            "Expected DownloadException to be thrown when response body is null"
        );
    }

    @Test
    void downloadFile_shouldThrowDownloadException_whenHttpClientThrowsIOException() throws Exception {
        // Given
        when(properties.getRequestTimeoutMinutes()).thenReturn(5);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofByteArray())))
                .thenThrow(new IOException("Test IO exception"));

        // When/Then
        var exception = assertThrows(DownloadException.class, () -> service.downloadFile(testUrl));
        assertTrue(exception.getMessage().contains("I/O error downloading"));
        assertEquals(IOException.class, exception.getCause().getClass());
    }

    @Test
    void downloadFile_shouldThrowDownloadException_whenResponseStatusCodeIsNotSuccess() throws Exception {
        // Given
        when(properties.getRequestTimeoutMinutes()).thenReturn(5);
        when(byteArrayResponse.statusCode()).thenReturn(404);
        when(byteArrayResponse.body()).thenReturn(testResponseBody);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofByteArray())))
                .thenReturn(byteArrayResponse);

        // When/Then
        assertThrows(
            DownloadException.class, 
            () -> service.downloadFile(testUrl),
            "Expected DownloadException to be thrown when status code is not success"
        );
    }

    @Test
    void downloadFile_toPath_shouldDownloadFile_whenHttpClientReturnsSuccessfulResponse() throws Exception {
        // Given
        when(properties.getFileRequestTimeoutMinutes()).thenReturn(30);
        when(fileResponse.statusCode()).thenReturn(200);
        when(fileResponse.body()).thenReturn(testDestinationPath);

        // Use doReturn().when() syntax to avoid strict stubbing argument mismatch
        doReturn(fileResponse).when(httpClient).send(any(HttpRequest.class), any());

        // When
        service.downloadFile(testUrl, testDestinationPath);

        // Then
        verify(httpClient).send(any(HttpRequest.class), any());
    }

    @Test
    void downloadFile_toPath_shouldThrowDownloadException_whenHttpClientThrowsIOException() throws Exception {
        // Given
        when(properties.getFileRequestTimeoutMinutes()).thenReturn(30);

        // Use doThrow().when() syntax to avoid strict stubbing argument mismatch
        doThrow(new IOException("Test IO exception"))
            .when(httpClient).send(any(HttpRequest.class), any());

        // When/Then
        var exception = assertThrows(
            DownloadException.class, 
            () -> service.downloadFile(testUrl, testDestinationPath),
            "Expected DownloadException to be thrown when HttpClient throws IOException"
        );
        assertTrue(exception.getMessage().contains("I/O error downloading"), 
                   "Exception message should mention I/O error");
        assertEquals(IOException.class, exception.getCause().getClass(), 
                     "Exception cause should be IOException");
    }

    @Test
    void downloadFile_toPath_shouldThrowDownloadException_whenResponseStatusCodeIsNotSuccess() throws Exception {
        // Given
        when(properties.getFileRequestTimeoutMinutes()).thenReturn(30);
        when(fileResponse.statusCode()).thenReturn(500);
        when(fileResponse.body()).thenReturn(testDestinationPath);

        // Use doReturn().when() syntax to avoid strict stubbing argument mismatch
        doReturn(fileResponse).when(httpClient).send(any(HttpRequest.class), any());

        // When/Then
        assertThrows(
            DownloadException.class, 
            () -> service.downloadFile(testUrl, testDestinationPath),
            "Expected DownloadException to be thrown when status code is not success"
        );
    }

    @Test
    void createHttpRequest_shouldCreateRequest_withCorrectUriAndTimeout() throws Exception {
        // Given
        var timeout = Duration.ofMinutes(10);

        // When
        var request = service.createHttpRequest(testUrl, timeout);

        // Then
        assertEquals(testUrl.toURI(), request.uri());
        assertEquals(timeout, request.timeout().orElseThrow());
        assertEquals("GET", request.method());
    }

    @Test
    void createHttpRequest_shouldThrowIllegalArgumentException_whenUrlIsNull() {
        var timeout = Duration.ofMinutes(5);

        // When/Then
        var exception = assertThrows(IllegalArgumentException.class, () -> service.createHttpRequest(null, timeout));
        assertEquals("URL cannot be null", exception.getMessage());
    }

    @Test
    void createHttpRequest_shouldThrowDownloadException_whenUrlHasInvalidSyntax() throws Exception {
        // Given
        var invalidUrl = new URL("http://invalid url with spaces");

        // When/Then
        var exception = assertThrows(DownloadException.class,
                () -> service.createHttpRequest(invalidUrl, Duration.ofMinutes(5)));
        assertTrue(exception.getMessage().contains("Invalid URL syntax"));
        assertEquals(URISyntaxException.class, exception.getCause().getClass());
    }

    @Test
    void validateResponse_shouldNotThrowException_whenStatusCodeIsSuccess() {
        // Given
        when(byteArrayResponse.statusCode()).thenReturn(200);

        // When/Then
        assertDoesNotThrow(() -> service.validateResponse(byteArrayResponse, testUrl, null));
    }

    @Test
    void validateResponse_shouldThrowDownloadException_whenStatusCodeIsNotSuccess() {
        // Given
        when(byteArrayResponse.statusCode()).thenReturn(404);
        when(byteArrayResponse.body()).thenReturn(testResponseBody);

        // When/Then
        var exception = assertThrows(DownloadException.class,
                () -> service.validateResponse(byteArrayResponse, testUrl, null));
        assertTrue(exception.getMessage().contains("HTTP request failed"));
        assertTrue(exception.getMessage().contains("Status Code: 404"));
    }

    @Test
    void ensureDirectoryExists_shouldCreateDirectories_whenParentDirectoryDoesNotExist() throws Exception {
        // Given
        var deepPath = tempDir.resolve("deep/nested/directory/file.txt");

        // When
        service.ensureDirectoryExists(deepPath);

        // Then
        assertTrue(Files.exists(deepPath.getParent()));
    }

    @Test
    void ensureDirectoryExists_shouldThrowDownloadException_whenDirectoryCreationFails() {
        // Given
        // Create a path that will be used in the test
        var testPath = tempDir.resolve("test-dir").resolve("file.txt");

        // Create a service that will throw an IOException when createDirectories is called
        var testService = new HttpClientFileDownloadService(httpClient, properties) {
            @Override
            protected void ensureDirectoryExists(Path destinationPath) throws DownloadException {
                throw new DownloadException("Failed to create directory structure for " + destinationPath);
            }
        };

        // When/Then
        assertThrows(
            DownloadException.class, 
            () -> testService.ensureDirectoryExists(testPath),
            "Expected DownloadException to be thrown when directory creation fails"
        );
    }

    @Test
    void cleanupFailedDownload_shouldDeleteFile_whenFileExists() throws Exception {
        // Given
        var existingFile = Files.createFile(tempDir.resolve("existing.txt"));

        // When
        service.cleanupFailedDownload(existingFile);

        // Then
        assertFalse(Files.exists(existingFile));
    }

    @Test
    void cleanupFailedDownload_shouldNotThrowException_whenFileDoesNotExist() {
        // Given
        var nonExistentFile = tempDir.resolve("non-existent.txt");

        // When/Then
        assertDoesNotThrow(() -> service.cleanupFailedDownload(nonExistentFile));
    }

    @Test
    void extractErrorBodySnippet_shouldReturnSnippet_whenBodyIsByteArray() {
        // Given
        var errorBody = "Error message".getBytes();
        when(byteArrayResponse.body()).thenReturn(errorBody);

        // When
        var result = service.extractErrorBodySnippet(byteArrayResponse);

        // Then
        assertEquals("Error message", result);
    }

    @Test
    void extractErrorBodySnippet_shouldReturnDefaultMessage_whenBodyIsNull() {
        // Given
        when(byteArrayResponse.body()).thenReturn(null);

        // When
        var result = service.extractErrorBodySnippet(byteArrayResponse);

        // Then
        assertEquals("[Could not retrieve body snippet]", result);
    }

    @Test
    void createDownloadException_shouldCreateSpecialMessage_forSSLHandshakeException() {
        // Given
        var message = "Download failed";
        IOException cause = new javax.net.ssl.SSLHandshakeException("SSL error");

        // When
        var result = service.createDownloadException(message, cause);

        // Then
        assertTrue(result.getMessage().contains("SSL handshake failed"));
        assertEquals(cause, result.getCause());
    }

    @Test
    void createDownloadException_shouldCreateRegularMessage_forOtherIOExceptions() {
        // Given
        var message = "Download failed";
        var cause = new IOException("IO error");

        // When
        var result = service.createDownloadException(message, cause);

        // Then
        assertEquals(message, result.getMessage());
        assertEquals(cause, result.getCause());
    }
}
