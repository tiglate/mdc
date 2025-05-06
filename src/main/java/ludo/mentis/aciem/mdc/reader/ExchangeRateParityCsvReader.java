package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.ExchangeRateParity;
import ludo.mentis.aciem.mdc.reader.editor.BrazilianBigDecimalEditor;
import ludo.mentis.aciem.mdc.reader.editor.CustomLocalDateEditor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ExchangeRateParityCsvReader extends FlatFileItemReader<ExchangeRateParity> {

    private static final int DEFAULT_LINES_TO_SKIP = 0; // No header lines to skip

    /**
     * Creates a new ExchangeRateParityCsvReader with the default configuration.
     * 
     * @param fileContent the content of the file to read
     * @param fileName the name of the file (used for error messages)
     */
    public ExchangeRateParityCsvReader(byte[] fileContent, String fileName) {
        this(fileContent, fileName, DEFAULT_LINES_TO_SKIP);
    }

    /**
     * Creates a new ExchangeRateParityCsvReader with a custom number of lines to skip.
     * 
     * @param fileContent the content of the file to read
     * @param fileName the name of the file (used for error messages)
     * @param linesToSkip the number of lines to skip at the beginning of the file
     */
    public ExchangeRateParityCsvReader(byte[] fileContent, String fileName, int linesToSkip) {
        super();

        if (fileContent == null) {
            throw new IllegalArgumentException("File content cannot be null");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (linesToSkip < 0) {
            throw new IllegalArgumentException("Lines to skip cannot be negative");
        }

        var fieldSetMapper = new BeanWrapperFieldSetMapper<ExchangeRateParity>();
        fieldSetMapper.setTargetType(ExchangeRateParity.class);
        fieldSetMapper.setCustomEditors(Map.of(
                LocalDate.class, new CustomLocalDateEditor("dd/MM/yyyy"),
                BigDecimal.class, new BrazilianBigDecimalEditor()
        ));

        var lineMapper = new DefaultLineMapper<ExchangeRateParity>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setResource(new ByteArrayResource(fileContent, fileName));
        setLinesToSkip(linesToSkip);
        setLineMapper(lineMapper);
    }

    private static DelimitedLineTokenizer getDelimitedLineTokenizer() {
        var tokenizer = new DelimitedLineTokenizer(";");
        tokenizer.setNames(
                "referenceDate",
                "currencyId",
                "type",
                "currencyCode",
                "buyRate",
                "sellRate",
                "buyParity",
                "sellParity");
        return tokenizer;
    }
}