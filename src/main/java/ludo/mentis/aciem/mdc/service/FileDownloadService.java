package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.model.HttpMethod;

import org.springframework.core.io.Resource;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

/**
 * Service interface for downloading files from URLs.
 */
public interface FileDownloadService {
    /**
     * Downloads a file from the specified URL and returns it as a Resource.
     *
     * @param url The URL to download from
     * @return The downloaded file as a Resource
     * @throws DownloadException If the download fails
     * @throws InterruptedException If the download is interrupted
     */
    Resource downloadFile(URL url) throws DownloadException, InterruptedException;

    /**
     * Downloads a file from the specified URL and saves it to the specified path.
     *
     * @param url The URL to download from
     * @param destinationPath The path to save the file to
     * @throws DownloadException If the download fails
     * @throws InterruptedException If the download is interrupted
     */
    void downloadFile(URL url, Path destinationPath) throws DownloadException, InterruptedException;
    
    /**
     * Downloads a file from the specified URL using the specified HTTP method and parameters,
     * and returns it as a Resource.
     *
     * @param url The URL to download from
     * @param method The HTTP method to use (GET or POST)
     * @param parameters The parameters to include in the request (query string for GET, form data for POST)
     * @return The downloaded file as a Resource
     * @throws DownloadException If the download fails
     * @throws InterruptedException If the download is interrupted
     */
    Resource downloadFile(URL url, HttpMethod method, Map<String, String> parameters) 
            throws DownloadException, InterruptedException;

    /**
     * Downloads a file from the specified URL using the specified HTTP method and parameters,
     * and saves it to the specified path.
     *
     * @param url The URL to download from
     * @param method The HTTP method to use (GET or POST)
     * @param parameters The parameters to include in the request (query string for GET, form data for POST)
     * @param destinationPath The path to save the file to
     * @throws DownloadException If the download fails
     * @throws InterruptedException If the download is interrupted
     */
    void downloadFile(URL url, HttpMethod method, Map<String, String> parameters, Path destinationPath) 
            throws DownloadException, InterruptedException;
}