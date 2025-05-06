package ludo.mentis.aciem.mdc.exception;

import org.springframework.aot.generate.Generated;

@Generated
public class HolidaysNotAvailableException extends Exception {
	
	private static final long serialVersionUID = 758382081049882056L;

	public HolidaysNotAvailableException(String message) {
        super(message);
    }

    public HolidaysNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
