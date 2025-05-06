package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.UpdatedNominalValue;
import ludo.mentis.aciem.mdc.reader.UpdatedNominalValueCsvReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.UpdatedNominalValueDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.UpdatedNominalValueExcelWriter;
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
public class UpdatedNominalValuesJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public UpdatedNominalValuesJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job updatedNominalValuesJob(Step downloadFileStepUNV, Step processFileStepUNV) {
        return new JobBuilder("UpdatedNominalValues", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepUNV)
                .next(processFileStepUNV)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepUNV(FileDownloadService fileDownloadService,
                             @Value("${updated-nominal-values.download-url}") String baseUrl,
                             @Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new UpdatedNominalValueDownloader(fileDownloadService, referenceDate, baseUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepUNV(@Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                            @Value("#{jobExecutionContext['referenceDate']}") LocalDate referenceDate,
                            @Value("${updated-nominal-values.output-dir}") String outputDir,
                            BackupService backupService,
                            ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<UpdatedNominalValue, UpdatedNominalValue>chunk(1000, this.transactionManager)
                .reader(new UpdatedNominalValueCsvReader(fileContent))
                .writer(new UpdatedNominalValueExcelWriter(backupService, excelHelper, referenceDate, outputDir))
                .build();
    }
}
