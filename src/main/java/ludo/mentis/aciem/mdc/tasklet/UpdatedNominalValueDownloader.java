package ludo.mentis.aciem.mdc.tasklet;

import ludo.mentis.aciem.mdc.service.FileDownloadService;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class UpdatedNominalValueDownloader extends BaseDownloaderTasklet {

	private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final String fileUrl;

	public UpdatedNominalValueDownloader(FileDownloadService fileDownloadService, LocalDate referenceDate,
			String fileUrl) {
		super(fileDownloadService, referenceDate);
		this.fileUrl = fileUrl;
	}

	@Override
    protected URL getFileUrl() throws MalformedURLException {
        return new URL(this.fileUrl);
    }

    @Override
    protected boolean usePostMethod() {
        return true;
    }

    @Override
    protected Map<String, String> getRequestParameters() {
        assert this.referenceDate != null;
        var parameters = new HashMap<String, String>();
        parameters.put("Data", this.referenceDate.format(FILE_DATE_FORMATTER));
        parameters.put("escolha", "2");
        parameters.put("Idioma", "US");
        parameters.put("saida", "csv");
        return parameters;
    }
}
