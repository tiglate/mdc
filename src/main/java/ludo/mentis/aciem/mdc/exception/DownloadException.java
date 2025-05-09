package ludo.mentis.aciem.mdc.exception;

import java.io.IOException;
import java.io.Serial;

import org.springframework.aot.generate.Generated;

@Generated
public class DownloadException extends IOException {
	
	@Serial
    private static final long serialVersionUID = 1164262395570561521L;

	public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}