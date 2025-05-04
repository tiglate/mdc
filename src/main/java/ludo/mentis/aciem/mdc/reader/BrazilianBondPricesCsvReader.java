package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BrazilianBondPricesCsvReader extends FlatFileItemReader<BrazilianBondPrice> {

    private static final int DEFAULT_LINES_TO_SKIP = 3; // Default number of header lines to skip

    /**
     * Creates a new BrazilianBondPricesCsvReader with the default configuration.
     * 
     * @param fileContent the content of the file to read
     * @param fileName the name of the file (used for error messages)
     */
    public BrazilianBondPricesCsvReader(byte[] fileContent, String fileName) {
        this(fileContent, fileName, DEFAULT_LINES_TO_SKIP);
    }

    /**
     * Creates a new BrazilianBondPricesCsvReader with a custom number of lines to skip.
     * 
     * @param fileContent the content of the file to read
     * @param fileName the name of the file (used for error messages)
     * @param linesToSkip the number of lines to skip at the beginning of the file
     */
    public BrazilianBondPricesCsvReader(byte[] fileContent, String fileName, int linesToSkip) {
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

        var fieldSetMapper = new BeanWrapperFieldSetMapper<BrazilianBondPrice>();
        fieldSetMapper.setTargetType(BrazilianBondPrice.class);
        fieldSetMapper.setCustomEditors(Map.of(
                LocalDate.class, new CustomLocalDateEditor("yyyyMMdd"),
                BigDecimal.class, new BrazilianBigDecimalEditor()
        ));

        var lineMapper = new DefaultLineMapper<BrazilianBondPrice>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setResource(new ByteArrayResource(fileContent, fileName));
        setLinesToSkip(linesToSkip);
        setLineMapper(lineMapper);
    }

    private static DelimitedLineTokenizer getDelimitedLineTokenizer() {
        var tokenizer = new DelimitedLineTokenizer("@");
        tokenizer.setNames("title",
                "referenceDate",
                "selicCode",
                "baseDate",
                "maturityDate",
                "buyRate",
                "sellRate",
                "indicativeRate",
                "price",
                "standardDeviation",
                "lowerIntervalD0",
                "upperIntervalD0",
                "lowerIntervalD1",
                "upperIntervalD1",
                "criteria");
        return tokenizer;
    }
}
