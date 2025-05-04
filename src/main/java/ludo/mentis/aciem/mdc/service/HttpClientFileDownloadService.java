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

    private final HttpClient httpClient;
    private final HttpClientProperties properties;

    // Inject the pre-configured HttpClient and properties
    public HttpClientFileDownloadService(HttpClient httpClient, HttpClientProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        log.info("HttpClientFileDownloadService initialized.");
    }

    @Override
    public Resource downloadFile(URL url) throws DownloadException, InterruptedException {
        HttpRequest request;
        try {
            request = buildRequest(url, Duration.ofMinutes(properties.getRequestTimeoutMinutes()));
        } catch (URISyntaxException e) {
            throw new DownloadException("Invalid URL syntax: " + url, e);
        }
        log.info("Sending request to download URL (to memory): {}", url);
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            handleResponseErrors(response, url, null);
            var body = response.body();
            log.info("Successfully received response for URL: {}, Size: {} bytes", url, body != null ? body.length : 0);
            if (body == null) {
                // Should not happen on success, but handle defensively
                throw new DownloadException("Download successful (status %d) but response body was null for URL: %s"
                        .formatted(response.statusCode(), url));
            }
            // Wrap a byte array into a Spring Resource
            return new ByteArrayResource(body);
        } catch (IOException e) {
            throw mapToDownloadException("I/O error downloading " + url, e);
        }
    }

    @Override
    public void downloadFile(URL url, Path destinationPath) throws DownloadException, InterruptedException {
        HttpRequest request;
        try {
            request = buildRequest(url, Duration.ofMinutes(properties.getFileRequestTimeoutMinutes()));
        } catch (URISyntaxException e) {
            throw new DownloadException("Invalid URL syntax: " + url, e);
        }
        try {
            var parentDir = destinationPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new DownloadException("Failed to create directory structure for " + destinationPath, e);
        }
        log.info("Sending request to download URL: {} to Path: {}", url, destinationPath);
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destinationPath));
            handleResponseErrors(response, url, destinationPath);
            log.info("Successfully downloaded file to: {}", response.body());
        } catch (IOException e) {
            throw mapToDownloadException("I/O error downloading " + url + " to " + destinationPath, e);
        }
    }

    private HttpRequest buildRequest(URL url, Duration timeout) throws URISyntaxException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
        return HttpRequest.newBuilder()
                .uri(url.toURI())
                .GET()
                .timeout(timeout)
                .build();
    }

    /**
     * Checks the HTTP response status code and throws an IOException for non-successful codes (outside 200-299).
     * Attempts to delete the target file if the download failed and a path was provided.
     */
    private void handleResponseErrors(HttpResponse<?> response, URL url, Path destinationPath)
            throws DownloadException {
        int statusCode = response.statusCode();
        // Consider 2xx status codes as a success
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        var bodySnippet = extractErrorBodySnippet(response);
        var errorMessage = String.format("HTTP request failed for URL: %s. Status Code: %d. Response Body Snippet: %s",
                url, statusCode, bodySnippet);
        log.warn(errorMessage);
        // Attempt to delete the potentially partial file if the download was to a path
        if (destinationPath != null && Files.exists(destinationPath)) {
            try {
                if (!Files.deleteIfExists(destinationPath)) {
                    log.warn("Deleted incomplete/erroneous file at: {}", destinationPath);
                }
            } catch (IOException deleteException) {
                log.warn("Failed to delete incomplete file {} after HTTP error {}: {}", destinationPath,
                        statusCode, deleteException.getMessage());
            }
        }
        throw new DownloadException(errorMessage);
    }

    private String extractErrorBodySnippet(HttpResponse<?> response) {
        try {
            if (response.body() instanceof byte[] bytes && bytes.length > 0) {
                return new String(bytes, 0, Math.min(bytes.length, 512)); // Limit snippet size
            } else if (response.body() instanceof String s && !s.isEmpty()) {
                return s.substring(0, Math.min(s.length(), 512));
            } else if (response.body() instanceof Path p) {
                // Avoid reading potentially large error files, just report the path
                return "[Error body content likely in file: " + p + "]";
            }
        } catch (Exception e) {
            log.warn("Could not extract error body snippet: {}", e.getMessage());
        }
        return "[Could not retrieve body snippet]";
    }

    private DownloadException mapToDownloadException(String message, IOException cause) {
        if (cause instanceof javax.net.ssl.SSLHandshakeException) {
            return new DownloadException(message + ". SSL handshake failed, check SSL configuration/trust.", cause);
        }
        return new DownloadException(message, cause);
    }
}