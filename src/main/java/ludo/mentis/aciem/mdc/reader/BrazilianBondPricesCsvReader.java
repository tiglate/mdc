package ludo.mentis.aciem.mdc.reader;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public class BrazilianBondPricesCsvReader extends FlatFileItemReader<BrazilianBondPrice> {

    public BrazilianBondPricesCsvReader(byte[] fileContent, String fileName) {
        super();

        var fieldSetMapper = new BeanWrapperFieldSetMapper<BrazilianBondPrice>();
        fieldSetMapper.setTargetType(BrazilianBondPrice.class);
        fieldSetMapper.setCustomEditors(Map.of(
                LocalDate.class, new CustomLocalDateEditor("yyyyMMdd"),
                BigDecimal.class, new CustomNumberEditor(BigDecimal.class,
                        NumberFormat.getInstance(new Locale("pt", "BR")), false)
        ));

        var lineMapper = new DefaultLineMapper<BrazilianBondPrice>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setResource(new ByteArrayResource(fileContent, fileName));
        setLinesToSkip(3); // skip headers
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
