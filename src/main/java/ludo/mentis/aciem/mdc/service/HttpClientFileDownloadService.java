package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.config.HttpClientProperties;
import ludo.mentis.aciem.mdc.exception.DownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class HttpClientFileDownloadService implements FileDownloadService {
    private static final Logger log = LoggerFactory.getLogger(HttpClientFileDownloadService.class);
    private static final int MAX_ERROR_SNIPPET_LENGTH = 512;
    private static final String ERROR_BODY_UNAVAILABLE = "[Could not retrieve body snippet]";

    private final HttpClient httpClient;
    private final HttpClientProperties properties;

    public HttpClientFileDownloadService(HttpClient httpClient, HttpClientProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        log.info("HttpClientFileDownloadService initialized.");
    }

    @Override
    public Resource downloadFile(URL url) throws DownloadException, InterruptedException {
        var request = createHttpRequest(url, Duration.ofMinutes(properties.getRequestTimeoutMinutes()));
        log.info("Sending request to download URL (to memory): {}", url);

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            validateResponse(response, url, null);

            var body = response.body();
            logSuccessfulDownload(url, body);

            if (body == null) {
                throw new DownloadException("Download successful (status %d) but response body was null for URL: %s"
                        .formatted(response.statusCode(), url));
            }
            return new ByteArrayResource(body);
        } catch (IOException e) {
            throw createDownloadException("I/O error downloading " + url, e);
        }
    }

    @Override
    public void downloadFile(URL url, Path destinationPath) throws DownloadException, InterruptedException {
        var request = createHttpRequest(url, Duration.ofMinutes(properties.getFileRequestTimeoutMinutes()));
        ensureDirectoryExists(destinationPath);

        log.info("Sending request to download URL: {} to Path: {}", url, destinationPath);
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destinationPath));
            validateResponse(response, url, destinationPath);
            log.info("Successfully downloaded file to: {}", response.body());
        } catch (IOException e) {
            throw createDownloadException("I/O error downloading " + url + " to " + destinationPath, e);
        }
    }

    protected HttpRequest createHttpRequest(URL url, Duration timeout) throws DownloadException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
        try {
            return HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .GET()
                    .timeout(timeout)
                    .build();
        } catch (URISyntaxException e) {
            throw new DownloadException("Invalid URL syntax: " + url, e);
        }
    }

    protected void validateResponse(HttpResponse<?> response, URL url, Path destinationPath) throws DownloadException {
        var statusCode = response.statusCode();
        if (HttpStatus.isSuccess(statusCode)) {
            return;
        }

        var bodySnippet = extractErrorBodySnippet(response);
        var errorMessage = String.format("HTTP request failed for URL: %s. Status Code: %d. Response Body Snippet: %s",
                url, statusCode, bodySnippet);
        log.warn(errorMessage);

        cleanupFailedDownload(destinationPath);
        throw new DownloadException(errorMessage);
    }

    protected void cleanupFailedDownload(Path destinationPath) {
        if (destinationPath == null || !Files.exists(destinationPath)) {
            return;
        }
        try {
            Files.deleteIfExists(destinationPath);
            log.warn("Deleted incomplete/erroneous file at: {}", destinationPath);
        } catch (IOException e) {
            log.warn("Failed to delete incomplete file {}: {}", destinationPath, e.getMessage());
        }
    }

    protected void ensureDirectoryExists(Path destinationPath) throws DownloadException {
        try {
            var parentDir = destinationPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new DownloadException("Failed to create directory structure for " + destinationPath, e);
        }
    }

    protected String extractErrorBodySnippet(HttpResponse<?> response) {
        try {
            var body = response.body();
            if (body instanceof byte[] bytes && bytes.length > 0) {
                return new String(bytes, 0, Math.min(bytes.length, MAX_ERROR_SNIPPET_LENGTH));
            } else if (body instanceof String s && !s.isEmpty()) {
                return s.substring(0, Math.min(s.length(), MAX_ERROR_SNIPPET_LENGTH));
            } else if (body instanceof Path p) {
                return "[Error body content likely in file: " + p + "]";
            }
        } catch (Exception e) {
            log.warn("Could not extract error body snippet: {}", e.getMessage());
        }
        return ERROR_BODY_UNAVAILABLE;
    }

    private void logSuccessfulDownload(URL url, byte[] body) {
        log.info("Successfully received response for URL: {}, Size: {} bytes", 
                url, body != null ? body.length : 0);
    }

    protected DownloadException createDownloadException(String message, IOException cause) {
        if (cause instanceof javax.net.ssl.SSLHandshakeException) {
            return new DownloadException(message + ". SSL handshake failed, check SSL configuration/trust.", cause);
        }
        return new DownloadException(message, cause);
    }

    private static class HttpStatus {
        private static final int SUCCESS_MIN = 200;
        private static final int SUCCESS_MAX = 299;

        static boolean isSuccess(int statusCode) {
            return statusCode >= SUCCESS_MIN && statusCode <= SUCCESS_MAX;
        }
    }
}
