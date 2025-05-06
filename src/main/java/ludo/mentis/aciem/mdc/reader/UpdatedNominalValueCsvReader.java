package ludo.mentis.aciem.mdc.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ByteArrayResource;

import ludo.mentis.aciem.mdc.model.UpdatedNominalValue;

public class UpdatedNominalValueCsvReader implements ItemReader<UpdatedNominalValue> {

    private final byte[] fileContent;
    private final List<UpdatedNominalValue> updatedNominalValues;
    private int currentIndex = 0;
    private boolean initialized = false;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public UpdatedNominalValueCsvReader(byte[] fileContent) {
        this.fileContent = fileContent;
        this.updatedNominalValues = new ArrayList<>();
    }

    @Override
    public UpdatedNominalValue read() throws Exception {
        if (!initialized) {
            initialize();
        }

        if (currentIndex < updatedNominalValues.size()) {
            return updatedNominalValues.get(currentIndex++);
        }
        return null; // End of data
    }

    private void initialize() throws IOException {
    	var resource = new ByteArrayResource(fileContent);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String[] headerFields = null;
            var referenceDate = LocalDate.now();

            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 2) {
            	
            	if (line.startsWith("Reference Date :")) {
            		referenceDate = this.parseReferenceDate(line);
            	} else if (line.startsWith("Titulo;SELIC Code;UNV")) {
            		headerFields = parseHeader(line);
            	} else if (headerFields != null && headerFields.length > 0) {
                    var item = parseLine(line);
                    if (item != null) {
                    	item.setReferenceDate(referenceDate);
                        this.updatedNominalValues.add(item);
                        lineCount++;
                    }	
            	}
            }
        }
        initialized = true;
    }
    
    private LocalDate parseReferenceDate(String line) {
        // Example of line "Reference Date :  05/06/2025"
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        // Extract the date part after the colon
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            return null;
        }
        
        var dateStr = line.substring(colonIndex + 1).trim();
        
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    private String[] parseHeader(String headerLine) throws IOException {
        if (headerLine == null) {
            throw new IOException("Empty file");
        }
        var fields = headerLine.split(";");
        if (fields.length != 6) {
            throw new IOException("Invalid header line: " + headerLine);
        }
        return fields;
    }

    private UpdatedNominalValue parseLine(String line) throws IOException {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        var fields = line.split(";");
        if (fields.length != 6) {
            throw new IOException("Invalid line: " + line);
        }
        try {
            var item = new UpdatedNominalValue();
            // Example of line: NTN-B;760199;4,499.253373;0.42;P;04/28/2025
            item.setSecurity(fields[0]);
            item.setSelicCode(fields[1]);
            item.setValue(new BigDecimal(fields[2].replace(",", "")));
            item.setIndex(new BigDecimal(fields[3].replace(",", "")));
            item.setReference(fields[4]);
            item.setValidSince(LocalDate.parse(fields[5], DATE_FORMATTER));
            return item;
        } catch (Exception ex) {
        	throw new IOException("Unable to parse line: " + line, ex);
        }
    }
}