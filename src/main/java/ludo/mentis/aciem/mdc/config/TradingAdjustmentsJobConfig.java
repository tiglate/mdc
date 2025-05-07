package ludo.mentis.aciem.mdc.config;

import java.time.LocalDate;

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

import ludo.mentis.aciem.mdc.model.TradingAdjustment;
import ludo.mentis.aciem.mdc.reader.TradingAdjustmentsHtmlReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.TradingAdjustmentsDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.TradingAdjustmentsExcelWriter;

@Configuration
public class TradingAdjustmentsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public TradingAdjustmentsJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job tradingAdjustmentsJob(Step downloadFileStepTAD, Step processFileStepTAD) {
        return new JobBuilder("TradingAdjustments", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepTAD)
                .next(processFileStepTAD)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepTAD(FileDownloadService fileDownloadService,
                             @Value("${trading-adjustments.download-url}") String fileUrl,
                             @Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new TradingAdjustmentsDownloader(fileDownloadService, referenceDate, fileUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepTAD(@Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                            @Value("#{jobExecutionContext['referenceDate']}") LocalDate referenceDate,
                            @Value("${trading-adjustments.output-dir}") String outputDir,
                            BackupService backupService,
                            ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<TradingAdjustment, TradingAdjustment>chunk(1000, this.transactionManager)
                .reader(new TradingAdjustmentsHtmlReader(fileContent))
                .writer(new TradingAdjustmentsExcelWriter(backupService, excelHelper, referenceDate, outputDir))
                .build();
    }
}
