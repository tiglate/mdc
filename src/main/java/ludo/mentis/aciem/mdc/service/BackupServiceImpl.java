package ludo.mentis.aciem.mdc.service;

import jakarta.annotation.PostConstruct;
import ludo.mentis.aciem.mdc.exception.HolidayLoadException;
import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BackupServiceImpl implements BackupService {

    private final HolidayManager holidayManager;
    private final BackupFileHandler fileHandler;

    public BackupServiceImpl(HolidayManager holidayManager, BackupFileHandler fileHandler) {
        this.holidayManager = holidayManager;
        this.fileHandler = fileHandler;
    }

    @PostConstruct
    private void initialize() throws HolidayLoadException {
        holidayManager.initialize();
    }

    @Override
    public void backup(String filePath) throws IOException, HolidaysNotAvailableException {
        backup(new BackupConfig(filePath));
    }

    @Override
    public void backup(String filePath, int daysBack) throws IOException, HolidaysNotAvailableException {
        backup(new BackupConfig(filePath, daysBack));
    }

    @Override
    public void backup(String filePath, int daysBack, boolean considerBusinessDays)
            throws IOException, HolidaysNotAvailableException {
        backup(new BackupConfig(filePath, daysBack, considerBusinessDays));
    }

    @Override
    public void backup(String filePath, String countryCode) throws IOException, HolidaysNotAvailableException {
        backup(new BackupConfig(filePath, countryCode));
    }

    @Override
    public void backup(String filePath, int daysBack, boolean considerBusinessDays, String countryCode)
            throws IOException, HolidaysNotAvailableException {
        backup(new BackupConfig(filePath, daysBack, considerBusinessDays, countryCode));
    }

    private void backup(BackupConfig config) throws IOException, HolidaysNotAvailableException {
        validateBackupConfig(config);
        
        var sourcePath = fileHandler.validateAndResolvePath(config.getFilePath());
        var targetDate = holidayManager.calculateTargetDate(
                config.getDaysBack(), 
                config.isConsiderBusinessDays(), 
                config.getCountryCode()
        );
        
        var targetPath = fileHandler.createBackupPath(sourcePath, targetDate);
        fileHandler.performBackup(sourcePath, targetPath);
    }

    private void validateBackupConfig(BackupConfig config) {
        if (config.getDaysBack() <= 0) {
            throw new IllegalArgumentException("daysBack must be positive.");
        }
    }

    // Inner configuration class
    private static class BackupConfig {
        private static final int DEFAULT_DAYS_BACK = 1;
        private static final String DEFAULT_COUNTRY_CODE = "BRA";
        private static final boolean DEFAULT_CONSIDER_BUSINESS_DAYS = true;

        private final String filePath;
        private final int daysBack;
        private final boolean considerBusinessDays;
        private final String countryCode;

        public BackupConfig(String filePath) {
            this(filePath, DEFAULT_DAYS_BACK, DEFAULT_CONSIDER_BUSINESS_DAYS, DEFAULT_COUNTRY_CODE);
        }

        public BackupConfig(String filePath, int daysBack) {
            this(filePath, daysBack, DEFAULT_CONSIDER_BUSINESS_DAYS, DEFAULT_COUNTRY_CODE);
        }

        public BackupConfig(String filePath, String countryCode) {
            this(filePath, DEFAULT_DAYS_BACK, DEFAULT_CONSIDER_BUSINESS_DAYS, countryCode);
        }

        public BackupConfig(String filePath, int daysBack, boolean considerBusinessDays) {
            this(filePath, daysBack, considerBusinessDays, DEFAULT_COUNTRY_CODE);
        }

        public BackupConfig(String filePath, int daysBack, boolean considerBusinessDays, String countryCode) {
            this.filePath = filePath;
            this.daysBack = daysBack;
            this.considerBusinessDays = considerBusinessDays;
            this.countryCode = countryCode;
        }

        // Getters
        public String getFilePath() { return filePath; }
        public int getDaysBack() { return daysBack; }
        public boolean isConsiderBusinessDays() { return considerBusinessDays; }
        public String getCountryCode() { return countryCode; }
    }
}