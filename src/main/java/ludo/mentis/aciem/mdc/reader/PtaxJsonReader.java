package ludo.mentis.aciem.mdc.reader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import ludo.mentis.aciem.mdc.model.Ptax;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A reader for Ptax JSON data that handles the nested structure of the JSON.
 */
public class PtaxJsonReader extends AbstractItemStreamItemReader<Ptax> {

    private final ObjectMapper objectMapper;
    private final Resource resource;
    private Iterator<Ptax> ptaxIterator;

    /**
     * Wrapper class for the Ptax JSON structure.
     */
    public static class PtaxWrapper {
        @JsonProperty("value")
        private List<Ptax> value;

        public List<Ptax> getValue() {
            return value;
        }

        public void setValue(List<Ptax> value) {
            this.value = value;
        }
    }

    public PtaxJsonReader(byte[] fileContent) {
        this.objectMapper = new ObjectMapper();
        this.resource = new ByteArrayResource(fileContent);

        // Configure date/time handling
        var javaTimeModule = new JavaTimeModule();
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
                    return new BigDecimal(value);
                } catch (Exception e) {
                    throw new JsonMappingException(parser, "Could not parse number: " + value, e);
                }
            }
        });
        objectMapper.registerModule(numberModule);

        // Configure general settings
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        try {
            PtaxWrapper wrapper = objectMapper.readValue(resource.getInputStream(), PtaxWrapper.class);
            if (wrapper != null && wrapper.getValue() != null) {
                ptaxIterator = wrapper.getValue().iterator();
            } else {
                ptaxIterator = Collections.emptyIterator();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Error reading Ptax data", e);
        }
    }

    @Override
    public Ptax read() {
        if (ptaxIterator != null && ptaxIterator.hasNext()) {
            return ptaxIterator.next();
        }
        return null;
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        // No state to update
    }

    @Override
    public void close() throws ItemStreamException {
        // No resources to close
    }
}
