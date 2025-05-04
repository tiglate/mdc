package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.DownloadException;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.nio.file.Path;

/**
 * Service interface for downloading files.
 */
public interface FileDownloadService {

    /**
     * Downloads the content of the given URL and returns it as a Spring Resource.
     * Note: This method may load the entire file content into memory depending on the implementation.
     * For large files, consider using downloadFile(URL url, Path destinationPath).
     *
     * @param url The URL of the file to download.
     * @return A Spring Resource containing the downloaded content.
     * @throws DownloadException If the download fails due to HTTP errors or I/O issues.
     * @throws InterruptedException If the operation is interrupted.
     */
    Resource downloadFile(URL url) throws DownloadException, InterruptedException;

    /**
     * Downloads the content of the given URL directly to the specified file path.
     * This method is generally more memory-efficient for large files.
     *
     * @param url             The URL of the file to download.
     * @param destinationPath The path where the downloaded file should be saved. Parent directories will be created if they don't exist.
     * @throws DownloadException If the download fails due to HTTP errors or I/O issues.
     * @throws InterruptedException If the operation is interrupted.
     */
    void downloadFile(URL url, Path destinationPath) throws DownloadException, InterruptedException;
}