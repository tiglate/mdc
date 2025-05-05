package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.FinancialIndicator;
import ludo.mentis.aciem.mdc.reader.FinancialIndicatorJsonReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.FinancialIndicatorDownloader;
import ludo.mentis.aciem.mdc.writer.FinancialIndicatorExcelWriter;
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

@Configuration
public class FinancialIndicatorsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public FinancialIndicatorsJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job financialIndicatorsJob(Step downloadFileStepFI, Step processFileStepFI) {
        return new JobBuilder("FinancialIndicators", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepFI)
                .next(processFileStepFI)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepFI(FileDownloadService fileDownloadService,
                            @Value("${financial-indicators.download-url}") String serviceUrl) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new FinancialIndicatorDownloader(fileDownloadService, serviceUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepFI(@Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                           @Value("${financial-indicators.output-dir}") String outputDir,
                           BackupService backupService) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<FinancialIndicator, FinancialIndicator>chunk(1000, this.transactionManager)
                .reader(new FinancialIndicatorJsonReader(fileContent))
                .writer(new FinancialIndicatorExcelWriter(backupService, outputDir))
                .build();
    }
}
