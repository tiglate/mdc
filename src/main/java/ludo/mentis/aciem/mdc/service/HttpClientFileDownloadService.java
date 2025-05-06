package ludo.mentis.aciem.mdc.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import ludo.mentis.aciem.mdc.config.HttpClientProperties;
import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.model.HttpMethod;

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
        return downloadFile(url, HttpMethod.GET, null);
    }

    @Override
    public void downloadFile(URL url, Path destinationPath) throws DownloadException, InterruptedException {
        downloadFile(url, HttpMethod.GET, null, destinationPath);
    }

    @Override
    public Resource downloadFile(URL url, HttpMethod method, Map<String, String> parameters) 
            throws DownloadException, InterruptedException {
        var request = createHttpRequest(url, method, parameters, 
                Duration.ofMinutes(properties.getRequestTimeoutMinutes()));
        log.info("Sending {} request to download URL (to memory): {}", method, url);

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
    public void downloadFile(URL url, HttpMethod method, Map<String, String> parameters, Path destinationPath) 
            throws DownloadException, InterruptedException {
        var request = createHttpRequest(url, method, parameters, 
                Duration.ofMinutes(properties.getFileRequestTimeoutMinutes()));
        ensureDirectoryExists(destinationPath);

        log.info("Sending {} request to download URL: {} to Path: {}", method, url, destinationPath);
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destinationPath));
            validateResponse(response, url, destinationPath);
            log.info("Successfully downloaded file to: {}", response.body());
        } catch (IOException e) {
            throw createDownloadException("I/O error downloading " + url + " to " + destinationPath, e);
        }
    }

    protected HttpRequest createHttpRequest(URL url, Duration timeout) throws DownloadException {
        return createHttpRequest(url, HttpMethod.GET, null, timeout);
    }

    protected HttpRequest createHttpRequest(URL url, HttpMethod method, Map<String, String> parameters, Duration timeout) 
            throws DownloadException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
        
        try {
            URI uri = url.toURI();
            
            // Handle GET with parameters by appending query string
            if (method == HttpMethod.GET && parameters != null && !parameters.isEmpty()) {
                String queryParams = buildQueryString(parameters);
                String originalUri = uri.toString();
                String separator = originalUri.contains("?") ? "&" : "?";
                uri = new URI(originalUri + separator + queryParams);
            }
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(timeout);
            
            // Handle POST with parameters by setting form body
            if (method == HttpMethod.POST && parameters != null && !parameters.isEmpty()) {
                String formData = buildQueryString(parameters);
                requestBuilder.header("Content-Type", "application/x-www-form-urlencoded")
                             .POST(HttpRequest.BodyPublishers.ofString(formData));
            } else if (method == HttpMethod.POST) {
                // POST with no parameters
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            } else {
                // Default to GET
                requestBuilder.GET();
            }
            
            return requestBuilder.build();
        } catch (URISyntaxException e) {
            throw new DownloadException("Invalid URL syntax: " + url, e);
        }
    }

    private String buildQueryString(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> encodeParameter(entry.getKey()) + "=" + encodeParameter(entry.getValue()))
                .collect(Collectors.joining("&"));
    }
    
    private String encodeParameter(String value) {
        if (value == null) {
            return "";
        }
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
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
