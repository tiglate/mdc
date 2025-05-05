package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.BrazilianBondPrice;
import ludo.mentis.aciem.mdc.reader.BrazilianBondPricesCsvReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.BrazilianBondPricesDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.BrazilianBondPricesExcelWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
public class BrazilianBondPricesJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BrazilianBondPricesJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job brazilianBondsPricesJob(Step downloadFileStepBBP, Step processFileStepBBP) {
        return new JobBuilder("BrazilianBondPrices", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepBBP)
                .next(processFileStepBBP)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepBBP(FileDownloadService fileDownloadService,
                             @Value("${brazilian-bond-prices.download-base-url}") String baseUrl,
                             @Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new BrazilianBondPricesDownloader(fileDownloadService, referenceDate, baseUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepBBP(@Value("#{jobExecutionContext['fileName']}") String fileName,
                            @Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                            @Value("#{jobExecutionContext['referenceDate']}") LocalDate referenceDate,
                            @Value("${brazilian-bond-prices.output-dir}") String outputDir,
                            BackupService backupService,
                            ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<BrazilianBondPrice, BrazilianBondPrice>chunk(1000, this.transactionManager)
                .reader(new BrazilianBondPricesCsvReader(fileContent, fileName))
                .writer(new BrazilianBondPricesExcelWriter(backupService, excelHelper, referenceDate, outputDir))
                .build();
    }
}
