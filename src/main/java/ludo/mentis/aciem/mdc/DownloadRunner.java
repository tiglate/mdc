package ludo.mentis.aciem.mdc;
/*
import ludo.mentis.aciem.mdc.exception.DownloadException;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

@Component // Make it a Spring bean
public class DownloadRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DownloadRunner.class);

    private final FileDownloadService fileDownloadService;

    // Inject the service
    public DownloadRunner(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @Override
    public void run(String... args) {
        log.info("Download Runner started...");

        try {
            // Example 1: Download a file directly to disk
            var largeFileUrl = new URL("https://www.anbima.com.br/informacoes/merc-sec/arqs/ms250429.txt");
            var destination = Paths.get("c:/temp/ms250429.txt");
            log.info("Attempting to download {} to {}...", largeFileUrl, destination);
            fileDownloadService.downloadFile(largeFileUrl, destination);
            log.info("Download to file complete.");

            // Example 2: Download a smaller file into memory as a Resource
            var textFileUrl = new URL("https://www.anbima.com.br/informacoes/merc-sec/arqs/ms250430.txt");
            log.info("Attempting to download {} to memory (Resource)...", textFileUrl);
            var resource = fileDownloadService.downloadFile(textFileUrl);
            log.info("Download to Resource complete. Resource description: {}", resource.getDescription());
            log.info("Resource content length: {} bytes", resource.contentLength());

            // Example of reading the resource content
            try (InputStream is = resource.getInputStream()) {
                String content = new String(is.readAllBytes());
                log.info("Resource Content Snippet:\n{}", content.substring(0, Math.min(content.length(), 250)) + "...");
            } catch (Exception e) {
                log.error("Failed to read downloaded resource content", e);
            }


        } catch (DownloadException | InterruptedException e) {
            log.error("A download error occurred:", e);
        } catch (MalformedURLException e) {
            log.error("Invalid URL used for download:", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred in DownloadRunner:", e);
        }

        log.info("Download Runner finished.");
    }
}*/