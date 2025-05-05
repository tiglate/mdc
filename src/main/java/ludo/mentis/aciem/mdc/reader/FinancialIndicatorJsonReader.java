package ludo.mentis.aciem.mdc.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FinancialIndicatorJsonReader extends JsonItemReader<FinancialIndicator> {

    public FinancialIndicatorJsonReader(byte[] fileContent) {
        var ptBR = new Locale("pt", "BR");
        var objectMapper = new ObjectMapper();

        // Configure date/time handling
        var javaTimeModule = new JavaTimeModule();
        var dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBR);
        var dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", ptBR);
        
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        objectMapper.registerModule(javaTimeModule);

        // Configure number handling
        var numberModule = new SimpleModule();
        numberModule.addDeserializer(BigDecimal.class, new JsonDeserializer<>() {
            @Override
            public BigDecimal deserialize(JsonParser parser, DeserializationContext context)
                    throws java.io.IOException {
                var value = parser.getText();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                try {
                    var symbols = new DecimalFormatSymbols(ptBR);
                    var decimalFormat = new DecimalFormat("#,##0.##", symbols);
                    decimalFormat.setParseBigDecimal(true);
                    return (BigDecimal) decimalFormat.parse(value);
                } catch (Exception e) {
                    throw new JsonMappingException(parser, "Could not parse number: " + value, e);
                }
            }
        });
        objectMapper.registerModule(numberModule);

        // Configure general settings
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Set up the reader
        setJsonObjectReader(new JacksonJsonObjectReader<>(objectMapper, FinancialIndicator.class));
        setResource(new ByteArrayResource(fileContent));
    }
}