package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;

import java.net.MalformedURLException;
import java.net.URL;

public class FinancialIndicatorDownloader extends BaseDownloaderTasklet {

    private final String serviceUrl;

    public FinancialIndicatorDownloader(FileDownloadService fileDownloadService, String serviceUrl) {
        super(fileDownloadService);
        this.serviceUrl = serviceUrl;
    }

    @Override
    protected URL getFileUrl() throws MalformedURLException {
        return new URL(serviceUrl);
    }
}
