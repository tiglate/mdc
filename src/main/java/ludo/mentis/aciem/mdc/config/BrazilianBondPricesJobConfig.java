package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.CustomLocalDateEditor;
import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.writer.BrazilianBondPricesExcelWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Configuration
public class BrazilianBondPricesJobConfig {

    private static final String FILE_PREFIX = "ms";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String BASE_URL = "http://localhost/anbima/";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FileDownloadService fileDownloadService;

    public BrazilianBondPricesJobConfig(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        FileDownloadService fileDownloadService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.fileDownloadService = fileDownloadService;
    }

    @Bean
    Job brazilianBondsPricesJob(Step downloadFileStep, Step processFileStep) {
        return new JobBuilder("BrazilianBondsPrices", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStep)
                .next(processFileStep)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStep(@Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFile", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    var date = referenceDate != null ? referenceDate : LocalDate.now();
                    var fileName = FILE_PREFIX + date.format(FILE_DATE_FORMATTER) + ".txt";
                    var fileUrl = new URL(BASE_URL + fileName);

                    var jobContext = contribution.getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    jobContext.put("fileName", fileName);
                    jobContext.put("fileContent", fileDownloadService.downloadFile(fileUrl).getContentAsByteArray());

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step processFileStep(FlatFileItemReader<BrazilianBondPrice> reader, ItemWriter<BrazilianBondPrice> writer) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<BrazilianBondPrice, BrazilianBondPrice>chunk(1000, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    FlatFileItemReader<BrazilianBondPrice> csvReader() {
        var fieldSetMapper = new BeanWrapperFieldSetMapper<BrazilianBondPrice>();
        fieldSetMapper.setTargetType(BrazilianBondPrice.class);
        fieldSetMapper.setCustomEditors(Map.of(
                LocalDate.class, new CustomLocalDateEditor("yyyyMMdd"),
                BigDecimal.class, new CustomNumberEditor(BigDecimal.class, NumberFormat.getInstance(new Locale("pt", "BR")), false)
        ));

        var lineMapper = new DefaultLineMapper<BrazilianBondPrice>();
        lineMapper.setLineTokenizer(getDelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(fieldSetMapper);

        var reader = new FlatFileItemReader<BrazilianBondPrice>();
        reader.setResource(new FileSystemResource("C:\\xampp\\htdocs\\anbima\\ms250430.txt"));
        reader.setLinesToSkip(3); // skip headers
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public ItemWriter<BrazilianBondPrice> excelWriter() {
        return new BrazilianBondPricesExcelWriter("C:\\temp\\");
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


    /*
    @Bean
    Step processFileStep() {
        return new StepBuilder("DownloadFile", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    var jobContext = contribution.getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    if (jobContext.get("fileContent") instanceof byte[] fileContent) {
                        var destinationPath = Path.of("c:", "temp").resolve(jobContext.getString("fileName"));
                        Files.write(destinationPath, fileContent);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
    */
}
