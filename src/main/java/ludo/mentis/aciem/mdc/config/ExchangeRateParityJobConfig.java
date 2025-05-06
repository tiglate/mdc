package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.ExchangeRateParity;
import ludo.mentis.aciem.mdc.reader.ExchangeRateParityCsvReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.ExchangeRateParityDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.ExchangeRateParityExcelWriter;
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
public class ExchangeRateParityJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ExchangeRateParityJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job exchangeRateParityJob(Step downloadFileStepERP, Step processFileStepERP) {
        return new JobBuilder("ExchangeRateParity", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepERP)
                .next(processFileStepERP)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepERP(FileDownloadService fileDownloadService,
                             @Value("${exchange-rate-parity.download-url}") String baseUrl,
                             @Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFileERP", this.jobRepository)
                .tasklet(new ExchangeRateParityDownloader(fileDownloadService, referenceDate, baseUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepERP(@Value("#{jobExecutionContext['fileName']}") String fileName,
                            @Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                            @Value("#{jobExecutionContext['referenceDate']}") LocalDate referenceDate,
                            @Value("${exchange-rate-parity.output-dir}") String outputDir,
                            BackupService backupService,
                            ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStepERP", jobRepository)
                .<ExchangeRateParity, ExchangeRateParity>chunk(1000, this.transactionManager)
                .reader(new ExchangeRateParityCsvReader(fileContent, fileName))
                .writer(new ExchangeRateParityExcelWriter(backupService, excelHelper, referenceDate, outputDir))
                .build();
    }
}