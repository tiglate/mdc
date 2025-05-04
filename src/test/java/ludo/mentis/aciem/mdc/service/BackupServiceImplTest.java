package ludo.mentis.aciem.mdc.service;

import ludo.mentis.aciem.mdc.exception.HolidaysNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceImplTest {

    @Mock
    private HolidayManager holidayManager;

    @Mock
    private BackupFileHandler fileHandler;

    @InjectMocks
    private BackupServiceImpl backupService;

    @TempDir
    Path tempDir;

    private String validFilePath;
    private Path resolvedPath;
    private Path backupPath;
    private final LocalDate targetDate = LocalDate.of(2023, 5, 15);

    @BeforeEach
    void setUp() throws IOException {
        // Create a test file in the temp directory
        Path testFile = Files.createFile(tempDir.resolve("file.txt"));
        validFilePath = testFile.toString();
        resolvedPath = testFile;

        // Create a backup path in the temp directory
        Path backupDir = tempDir.resolve(Paths.get("2023", "05", "15"));
        Files.createDirectories(backupDir);
        backupPath = backupDir.resolve("file.txt");
    }

    @Test
    void backup_withFilePath_shouldUseDefaultValues() throws IOException, HolidaysNotAvailableException {
        // Given
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(1, true, "BRA")).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);

        // When
        backupService.backup(validFilePath);

        // Then
        verify(holidayManager).calculateTargetDate(1, true, "BRA");
        verify(fileHandler).validateAndResolvePath(validFilePath);
        verify(fileHandler).createBackupPath(resolvedPath, targetDate);
        verify(fileHandler).performBackup(resolvedPath, backupPath);
    }

    @Test
    void backup_withFilePathAndDaysBack_shouldUseSpecifiedDaysBack() throws IOException, HolidaysNotAvailableException {
        // Given
        int daysBack = 5;
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(daysBack, true, "BRA")).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);

        // When
        backupService.backup(validFilePath, daysBack);

        // Then
        verify(holidayManager).calculateTargetDate(daysBack, true, "BRA");
        verify(fileHandler).validateAndResolvePath(validFilePath);
        verify(fileHandler).createBackupPath(resolvedPath, targetDate);
        verify(fileHandler).performBackup(resolvedPath, backupPath);
    }

    @Test
    void backup_withFilePathDaysBackAndConsiderBusinessDays_shouldUseSpecifiedValues() throws IOException, HolidaysNotAvailableException {
        // Given
        int daysBack = 3;
        boolean considerBusinessDays = false;
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(daysBack, considerBusinessDays, "BRA")).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);

        // When
        backupService.backup(validFilePath, daysBack, considerBusinessDays);

        // Then
        verify(holidayManager).calculateTargetDate(daysBack, considerBusinessDays, "BRA");
        verify(fileHandler).validateAndResolvePath(validFilePath);
        verify(fileHandler).createBackupPath(resolvedPath, targetDate);
        verify(fileHandler).performBackup(resolvedPath, backupPath);
    }

    @Test
    void backup_withFilePathAndCountryCode_shouldUseSpecifiedCountryCode() throws IOException, HolidaysNotAvailableException {
        // Given
        String customCountryCode = "USA";
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(1, true, customCountryCode)).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);

        // When
        backupService.backup(validFilePath, customCountryCode);

        // Then
        verify(holidayManager).calculateTargetDate(1, true, customCountryCode);
        verify(fileHandler).validateAndResolvePath(validFilePath);
        verify(fileHandler).createBackupPath(resolvedPath, targetDate);
        verify(fileHandler).performBackup(resolvedPath, backupPath);
    }

    @Test
    void backup_withAllParameters_shouldUseAllSpecifiedValues() throws IOException, HolidaysNotAvailableException {
        // Given
        int daysBack = 7;
        boolean considerBusinessDays = false;
        String customCountryCode = "USA";
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(daysBack, considerBusinessDays, customCountryCode)).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);

        // When
        backupService.backup(validFilePath, daysBack, considerBusinessDays, customCountryCode);

        // Then
        verify(holidayManager).calculateTargetDate(daysBack, considerBusinessDays, customCountryCode);
        verify(fileHandler).validateAndResolvePath(validFilePath);
        verify(fileHandler).createBackupPath(resolvedPath, targetDate);
        verify(fileHandler).performBackup(resolvedPath, backupPath);
    }

    @Test
    void backup_shouldThrowIllegalArgumentException_whenDaysBackIsZero() {
        // No setup needed - validation should fail before any mocked methods are called

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> backupService.backup(validFilePath, 0));
    }

    @Test
    void backup_shouldThrowIllegalArgumentException_whenDaysBackIsNegative() {
        // No setup needed - validation should fail before any mocked methods are called

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> backupService.backup(validFilePath, -1));
    }

    @Test
    void backup_shouldPropagateNoSuchFileException_fromFileHandler() throws IOException {
        // Given
        String invalidFilePath = "nonexistent.txt";
        when(fileHandler.validateAndResolvePath(invalidFilePath))
                .thenThrow(new NoSuchFileException("File not found"));

        // When/Then
        assertThrows(NoSuchFileException.class, () -> backupService.backup(invalidFilePath));
    }

    @Test
    void backup_shouldPropagateIOException_fromFileHandler() throws IOException, HolidaysNotAvailableException {
        // Given
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(anyInt(), anyBoolean(), anyString())).thenReturn(targetDate);
        when(fileHandler.createBackupPath(resolvedPath, targetDate)).thenReturn(backupPath);
        doThrow(new IOException("IO error")).when(fileHandler).performBackup(resolvedPath, backupPath);

        // When/Then
        assertThrows(IOException.class, () -> backupService.backup(validFilePath));
    }

    @Test
    void backup_shouldPropagateHolidaysNotAvailableException_fromHolidayManager() throws IOException, HolidaysNotAvailableException {
        // Given
        when(fileHandler.validateAndResolvePath(validFilePath)).thenReturn(resolvedPath);
        when(holidayManager.calculateTargetDate(anyInt(), anyBoolean(), anyString()))
                .thenThrow(new HolidaysNotAvailableException("No holidays for country"));

        // When/Then
        assertThrows(HolidaysNotAvailableException.class, () -> backupService.backup(validFilePath));
    }
}
