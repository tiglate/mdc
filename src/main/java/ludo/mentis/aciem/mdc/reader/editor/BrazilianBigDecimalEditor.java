package ludo.mentis.aciem.mdc.reader.editor;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.lang.NonNull;

import java.text.NumberFormat;
import java.util.Locale;

public class BrazilianBigDecimalEditor extends CustomNumberEditor {

    public BrazilianBigDecimalEditor() {
        super(java.math.BigDecimal.class, NumberFormat.getInstance(new Locale("pt", "BR")), true);
    }

    @Override
    public void setAsText(@NonNull String text) throws IllegalArgumentException {
        if ("--".equalsIgnoreCase(text)) {
            setValue(null);
            return;
        }
        super.setAsText(text);
    }
}
