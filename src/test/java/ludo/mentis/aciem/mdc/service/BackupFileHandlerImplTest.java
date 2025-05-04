package ludo.mentis.aciem.mdc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BackupFileHandlerImplTest {

    @InjectMocks
    private BackupFileHandlerImpl backupFileHandler;

    @TempDir
    Path tempDir;

    private Path testFile;
    private Path testDirectory;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test file in the temp directory
        testFile = Files.createFile(tempDir.resolve("testFile.txt"));
        // Create a test directory in the temp directory
        testDirectory = Files.createDirectory(tempDir.resolve("testDirectory"));
    }

    @Test
    void validateAndResolvePath_shouldReturnPath_whenFileExists() throws NoSuchFileException {
        // Given
        var filePath = testFile.toString();

        // When
        var result = backupFileHandler.validateAndResolvePath(filePath);

        // Then
        assertEquals(testFile, result);
    }

    @Test
    void validateAndResolvePath_shouldThrowNoSuchFileException_whenFileDoesNotExist() {
        // Given
        var filePath = tempDir.resolve("nonExistentFile.txt").toString();

        // When/Then
        assertThrows(NoSuchFileException.class, () -> backupFileHandler.validateAndResolvePath(filePath));
    }

    @Test
    void validateAndResolvePath_shouldThrowIllegalArgumentException_whenPathIsDirectory() {
        // Given
        var directoryPath = testDirectory.toString();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> backupFileHandler.validateAndResolvePath(directoryPath));
    }


    @Test
    void createBackupPath_shouldCreateCorrectPath_withValidInputs() {
        // Given
        var targetDate = LocalDate.of(2023, 5, 15);

        // When
        var result = backupFileHandler.createBackupPath(testFile, targetDate);

        // Then
        // Use Path.resolve to handle OS-specific path separators
        var expectedPath = testFile.getParent()
                .resolve("2023")
                .resolve("05")
                .resolve("15")
                .resolve(testFile.getFileName());
        assertEquals(expectedPath.toString(), result.toString());
    }

    @Test
    void createBackupPath_shouldHandleNullParent_byUsingCurrentDirectory() {
        // Given
        var targetDate = LocalDate.of(2023, 5, 15);
        var fileWithoutParent = Paths.get("fileWithoutParent.txt");

        // When
        var result = backupFileHandler.createBackupPath(fileWithoutParent, targetDate);

        // Then
        // The result should contain the date path and the filename
        // Use the correct path separator for the OS
        var expectedPathPart = String.format("2023%s05%s15%sfileWithoutParent.txt",
                                               File.separator, File.separator, File.separator);
        assertTrue(result.toString().contains(expectedPathPart), 
                   "Path should contain " + expectedPathPart + " but was " + result.toString());
    }

    @Test
    void performBackup_shouldMoveFile_whenTargetDirectoryDoesNotExist() throws IOException {
        // Given
        var sourceFile = Files.createFile(tempDir.resolve("sourceFile.txt"));
        var targetDir = tempDir.resolve("backup/2023/05/15");
        var targetFile = targetDir.resolve("sourceFile.txt");

        // When
        backupFileHandler.performBackup(sourceFile, targetFile);

        // Then
        assertFalse(Files.exists(sourceFile), "Source file should no longer exist");
        assertTrue(Files.exists(targetFile), "Target file should exist");
    }

    @Test
    void performBackup_shouldReplaceExistingFile_whenTargetFileExists() throws IOException {
        // Given
        var sourceFile = Files.createFile(tempDir.resolve("sourceFile.txt"));
        Files.write(sourceFile, "source content".getBytes());

        var targetDir = tempDir.resolve("backup/2023/05/15");
        Files.createDirectories(targetDir);
        var targetFile = targetDir.resolve("sourceFile.txt");
        Files.createFile(targetFile);
        Files.write(targetFile, "target content".getBytes());

        // When
        backupFileHandler.performBackup(sourceFile, targetFile);

        // Then
        assertFalse(Files.exists(sourceFile), "Source file should no longer exist");
        assertTrue(Files.exists(targetFile), "Target file should exist");
    }

    @Test
    void performBackup_shouldThrowIOException_whenSourceFileDoesNotExist() {
        // Given
        var nonExistentSource = tempDir.resolve("nonExistentSource.txt");
        var targetFile = tempDir.resolve("backup/target.txt");

        // When/Then
        assertThrows(IOException.class, () -> backupFileHandler.performBackup(nonExistentSource, targetFile));
    }
}
