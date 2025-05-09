package ludo.mentis.aciem.mdc.exception;

import org.springframework.aot.generate.Generated;

import java.io.Serial;

@Generated
public class HolidaysNotAvailableException extends Exception {
	
	@Serial
    private static final long serialVersionUID = 758382081049882056L;

	public HolidaysNotAvailableException(String message) {
        super(message);
    }

    public HolidaysNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
