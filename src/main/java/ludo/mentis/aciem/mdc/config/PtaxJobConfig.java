package ludo.mentis.aciem.mdc.config;

import ludo.mentis.aciem.mdc.model.Ptax;
import ludo.mentis.aciem.mdc.reader.PtaxJsonReader;
import ludo.mentis.aciem.mdc.service.BackupService;
import ludo.mentis.aciem.mdc.service.FileDownloadService;
import ludo.mentis.aciem.mdc.tasklet.PtaxDownloader;
import ludo.mentis.aciem.mdc.util.ExcelHelper;
import ludo.mentis.aciem.mdc.writer.PtaxExcelWriter;
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
public class PtaxJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public PtaxJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    Job ptaxJob(Step downloadFileStepPX, Step processFileStepPX) {
        return new JobBuilder("Ptax", this.jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downloadFileStepPX)
                .next(processFileStepPX)
                .build();
    }

    @Bean
    @JobScope
    Step downloadFileStepPX(FileDownloadService fileDownloadService,
                            @Value("${ptax.download-url}") String serviceUrl) {
        return new StepBuilder("DownloadFile", this.jobRepository)
                .tasklet(new PtaxDownloader(fileDownloadService, serviceUrl),
                        this.transactionManager)
                .build();
    }

    @Bean
    @JobScope
    Step processFileStepPX(@Value("#{jobExecutionContext['fileContent']}") byte[] fileContent,
                           @Value("${ptax.output-dir}") String outputDir,
                           BackupService backupService,
                           ExcelHelper excelHelper) {
        return new StepBuilder("ProcessFileStep", jobRepository)
                .<Ptax, Ptax>chunk(1000, this.transactionManager)
                .reader(new PtaxJsonReader(fileContent))
                .writer(new PtaxExcelWriter(backupService, excelHelper, outputDir))
                .build();
    }
}
