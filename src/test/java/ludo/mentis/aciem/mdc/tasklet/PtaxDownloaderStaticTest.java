package ludo.mentis.aciem.mdc.tasklet;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PtaxDownloaderStaticTest {

    @Test
    void constructDownloadUrl_shouldFormatUrlCorrectly() {
        // Given
        var baseUrl = "https://example.com/api?startDate=%s&endDate=%s";
        
        // When
        var formattedUrl = PtaxDownloader.constructDownloadUrl(baseUrl);
        
        // Then
        // Get the expected dates
        var endDate = LocalDate.now();
        var startDate = endDate.minusDays(30);
        var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        var expectedUrl = String.format(baseUrl, 
                                       startDate.format(formatter), 
                                       endDate.format(formatter));
        
        assertEquals(expectedUrl, formattedUrl);
    }
}