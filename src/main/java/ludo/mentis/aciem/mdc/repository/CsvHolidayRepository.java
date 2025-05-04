package ludo.mentis.aciem.mdc.repository;

import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.InvalidFileFormatException;
import ludo.mentis.aciem.mdc.model.Holiday;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of HolidayRepository that loads holidays from a CSV file.
 */
@Repository
public class CsvHolidayRepository implements HolidayRepository {

    private static final Logger log = LoggerFactory.getLogger(CsvHolidayRepository.class);
    private static final String HOLIDAYS_FILE = "holidays.csv";
    private static final DateTimeFormatter DATE_FORMATTER_CSV = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Override
    public List<Holiday> findAll() throws HolidayLoadException {
        log.info("Loading holidays from {}...", HOLIDAYS_FILE);
        try {
            var holidayList = readHolidaysFromFile();
            log.info("Successfully parsed {} holiday records from {}.", holidayList.size(), HOLIDAYS_FILE);
            return holidayList;
        } catch (IOException e) {
            throw new HolidayLoadException("Failed to read holidays file: " + HOLIDAYS_FILE, e);
        } catch (InvalidFileFormatException e) {
            throw new HolidayLoadException("Invalid holiday file format: " + HOLIDAYS_FILE, e);
        }
    }

    /**
     * Reads and parses holidays from a CSV file in the classpath.
     *
     * @return List of Holiday objects parsed from the CSV file
     * @throws IOException                if there is an error reading the file
     * @throws InvalidFileFormatException if the file format is invalid or contains malformed data
     */
    protected List<Holiday> readHolidaysFromFile() throws IOException, InvalidFileFormatException {
        try (var inputStream = getHolidayFileStream();
             var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             var reader = new BufferedReader(streamReader)) {

            validateHeader(reader);
            return processHolidayLines(reader);
        }
    }

    /**
     * Retrieves an InputStream for reading the holidays CSV file from the classpath.
     *
     * @return InputStream connected to the holidays CSV file
     * @throws IOException if the file cannot be found in the classpath, or if there's an error accessing it
     */
    protected InputStream getHolidayFileStream() throws IOException {
        var inputStream = getClass().getClassLoader().getResourceAsStream(HOLIDAYS_FILE);
        if (inputStream == null) {
            throw new IOException(HOLIDAYS_FILE + " not found in classpath");
        }
        return inputStream;
    }

    /**
     * Processes the content of the CSV file line by line, converting valid lines into Holiday objects.
     *
     * @param reader BufferedReader positioned after the header line, ready to read holiday entries
     * @return List of Holiday objects parsed from the file
     * @throws IOException                if there is an error reading from the file
     * @throws InvalidFileFormatException if any line in the file has invalid format or contains malformed data
     */
    protected List<Holiday> processHolidayLines(BufferedReader reader) throws IOException, InvalidFileFormatException {
        var holidayList = new ArrayList<Holiday>();
        int lineNum = 1;
        String line;

        while ((line = reader.readLine()) != null) {
            lineNum++;
            if (shouldProcessLine(line)) {
                holidayList.add(parseLine(line.trim(), lineNum));
            }
        }
        return holidayList;
    }

    /**
     * Determines whether a line from the CSV file should be processed.
     * Skips empty lines and lines starting with '#' (comments).
     *
     * @param line the line to check
     * @return true if the line should be processed, false otherwise
     */
    protected boolean shouldProcessLine(String line) {
        var trimmedLine = line.trim();
        return !trimmedLine.isEmpty() && !trimmedLine.startsWith("#");
    }

    /**
     * Validates the header of the CSV file.
     *
     * @param reader BufferedReader positioned at the start of the file
     * @throws IOException if there is an error reading the file
     * @throws InvalidFileFormatException if the header is missing or invalid
     */
    protected void validateHeader(BufferedReader reader) throws IOException, InvalidFileFormatException {
        var header = reader.readLine();
        if (header == null) {
            throw new InvalidFileFormatException("Holiday file " + HOLIDAYS_FILE + " is empty");
        }
        if (!header.trim().toLowerCase().startsWith("date,")) {
            throw new InvalidFileFormatException("Holiday file " + HOLIDAYS_FILE + 
                    " has an invalid header. Expected header starting with 'Date,'");
        }
    }

    /**
     * Parses a single line from the CSV file into a Holiday object.
     *
     * @param line The line to parse
     * @param lineNum The line number for error reporting
     * @return A Holiday object
     * @throws InvalidFileFormatException if the line format is invalid
     */
    protected Holiday parseLine(String line, int lineNum) throws InvalidFileFormatException {
        var parts = line.split(",", 3); // Split into 3 parts max
        if (parts.length < 3) {
            throw new InvalidFileFormatException(
                    "Malformed line %d in %s: Insufficient columns - %s".formatted(lineNum, HOLIDAYS_FILE, line));
        }
        try {
            var date = LocalDate.parse(parts[0].trim(), DATE_FORMATTER_CSV);
            var location = parts[1].trim();
            var description = parts[2].trim();
            return new Holiday(date, location, description);
        } catch (DateTimeParseException e) {
            throw new InvalidFileFormatException(
                    "Invalid date format at line %d in %s: '%s'".formatted(lineNum, HOLIDAYS_FILE, parts[0]), e);
        } catch (IllegalArgumentException e) {
            throw new InvalidFileFormatException(
                    "Invalid holiday data at line %d in %s: %s".formatted(lineNum, HOLIDAYS_FILE, e.getMessage()), e);
        } catch (Exception e) {
            throw new InvalidFileFormatException(
                    "Error processing line %d in %s: %s".formatted(lineNum, HOLIDAYS_FILE, e.getMessage()), e);
        }
    }
}
