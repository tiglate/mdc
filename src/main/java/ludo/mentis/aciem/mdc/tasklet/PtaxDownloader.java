package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PtaxDownloader extends BaseDownloaderTasklet {

    private final String serviceUrl;

    public PtaxDownloader(FileDownloadService fileDownloadService, String serviceUrl) {
        super(fileDownloadService);
        this.serviceUrl = constructDownloadUrl(serviceUrl);
    }

    @Override
    protected URL getFileUrl() throws MalformedURLException {
        return new URL(serviceUrl);
    }

    /**
     * Constructs the URL for downloading Ptax data.
     *
     * @param downloadUrl The base URL pattern from application.properties
     * @return The formatted URL with date parameters
     */
    public static String constructDownloadUrl(String downloadUrl) {
        var endDate = LocalDate.now();
        var startDate = endDate.minusDays(30);

        var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        var formattedStartDate = startDate.format(formatter);
        var formattedEndDate = endDate.format(formatter);

        return String.format(downloadUrl, formattedStartDate, formattedEndDate);
    }
}
