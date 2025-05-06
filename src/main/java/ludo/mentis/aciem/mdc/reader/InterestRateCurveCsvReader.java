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

import ludo.mentis.aciem.mdc.model.InterestRateCurve;

public class InterestRateCurveCsvReader implements ItemReader<InterestRateCurve> {

    private final byte[] fileContent;
    private final List<InterestRateCurve> interestRateCurves;
    private int currentIndex = 0;
    private boolean initialized = false;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InterestRateCurveCsvReader(byte[] fileContent) {
        this.fileContent = fileContent;
        this.interestRateCurves = new ArrayList<>();
    }

    @Override
    public InterestRateCurve read() throws Exception {
        if (!initialized) {
            initialize();
        }

        if (currentIndex < interestRateCurves.size()) {
            return interestRateCurves.get(currentIndex++);
        }
        return null; // End of data
    }

    private void initialize() throws IOException {
    	var resource = new ByteArrayResource(fileContent);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            // Read header line (first line)
            var headerFields = parseHeader(reader.readLine());
            var referenceDate = LocalDate.parse(headerFields[0], DATE_FORMATTER);
            
            // Read data lines (only interested in the next 2 lines)
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 2) {
                var curve = parseLine(line);
                if (curve != null) {
                	curve.setReferenceDate(referenceDate);
                    interestRateCurves.add(curve);
                    lineCount++;
                }
            }
        }
        initialized = true;
    }
    
    private String[] parseHeader(String headerLine) throws IOException {
        if (headerLine == null) {
            throw new IOException("Empty file");
        }
        var fields = headerLine.split(";");
        if (fields.length != 7) {
            throw new IOException("Invalid header line: " + headerLine);
        }
        return fields;
    }

    private InterestRateCurve parseLine(String line) throws IOException {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        var fields = line.split(";");
        if (fields.length != 7) {
            throw new IOException("Invalid line: " + line);
        }
        var curve = new InterestRateCurve();
        // Example of line: PREFIXADOS;7.96287626860661E-02;5.96513971347563E-02;8.64348297182261E-02;0.181498016176063;2.03671405327417;0.177752217216416
        curve.setDescription(fields[0]);
        curve.setBeta1(new BigDecimal(fields[1]));
        curve.setBeta2(new BigDecimal(fields[2]));
        curve.setBeta3(new BigDecimal(fields[3]));
        curve.setBeta4(new BigDecimal(fields[4]));
        curve.setLambda1(new BigDecimal(fields[5]));
        curve.setLambda2(new BigDecimal(fields[6]));
        return curve;
    }
}