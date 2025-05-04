package ludo.mentis.aciem.mdc.exception;

import java.io.IOException;

public class DownloadException extends IOException {
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}