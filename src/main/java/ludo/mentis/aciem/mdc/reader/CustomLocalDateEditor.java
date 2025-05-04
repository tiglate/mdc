package ludo.mentis.aciem.mdc.reader;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateEditor extends PropertyEditorSupport {
    private final DateTimeFormatter formatter;

    public CustomLocalDateEditor(String pattern) {
        this(DateTimeFormatter.ofPattern(pattern));
    }

    public CustomLocalDateEditor(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void setAsText(String text) {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
        } else {
            setValue(LocalDate.parse(text, formatter));
        }
    }

    @Override
    public String getAsText() {
        var value = (LocalDate) getValue();
        return value == null ? "" : formatter.format(value);
    }
}