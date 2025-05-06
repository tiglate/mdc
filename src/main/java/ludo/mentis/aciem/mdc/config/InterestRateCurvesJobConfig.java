package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.InterestRateCurve;
import ludo.mentis.aciem.mdc.reader.InterestRateCurveCsvReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.InterestRateCurveDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.InterestRateCurveExcelWriter;
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
public class InterestRateCurvesJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public InterestRateCurvesJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job interestRateCurvesJob(Step downloadFileStepIRC, Step processFileStepIRC) {
        return new JobBuilder("InterestRateCurves", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepIRC)
                .next(processFileStepIRC)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepIRC(FileDownloadService fileDownloadService,
                             @Value("${interest-rate-curve.download-url}") String baseUrl,
                             @Value("#{jobParameters['referenceDate'] ?: null}") LocalDate referenceDate) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new InterestRateCurveDownloader(fileDownloadService, referenceDate, baseUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepIRC(@Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                            @Value("#{jobExecutionContext['referenceDate']}") LocalDate referenceDate,
                            @Value("${interest-rate-curve.output-dir}") String outputDir,
                            BackupService backupService,
                            ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<InterestRateCurve, InterestRateCurve>chunk(1000, this.transactionManager)
                .reader(new InterestRateCurveCsvReader(fileContent))
                .writer(new InterestRateCurveExcelWriter(backupService, excelHelper, referenceDate, outputDir))
                .build();
    }
}
