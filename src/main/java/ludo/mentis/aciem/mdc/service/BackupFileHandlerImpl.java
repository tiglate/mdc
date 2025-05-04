package ludo.mentis.aciem.mdc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BackupFileHandlerImpl implements BackupFileHandler {
    private static final DateTimeFormatter DATE_FORMATTER_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Logger log = LoggerFactory.getLogger(BackupFileHandlerImpl.class);

    @Override
    public Path validateAndResolvePath(String filePath) throws NoSuchFileException {
        var sourcePath = Paths.get(filePath);
        validateSourcePath(sourcePath);
        return resolveSourcePath(sourcePath);
    }

    private void validateSourcePath(Path sourcePath) throws NoSuchFileException {
        if (!Files.exists(sourcePath)) {
            throw new NoSuchFileException("Source file not found: " + sourcePath);
        }
        if (Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("Source path must be a file, not a directory: " + sourcePath);
        }
    }

    private Path resolveSourcePath(Path sourcePath) throws NoSuchFileException {
        if (sourcePath.getParent() != null) {
            return sourcePath;
        }

        var currentDirectory = Paths.get(".").toAbsolutePath().getParent();
        log.warn("Source file '{}' has no parent directory. Using current working directory '{}' as base.",
                sourcePath, currentDirectory);

        if (!sourcePath.isAbsolute()) {
            var resolvedPath = currentDirectory.resolve(sourcePath.getFileName());
            if (!Files.exists(resolvedPath)) {
                throw new NoSuchFileException("Source file not found after resolving relative path: " + resolvedPath);
            }
            return resolvedPath;
        }

        return sourcePath;
    }

    @Override
    public Path createBackupPath(Path sourcePath, LocalDate targetDate) {
        var sourceDirectory = sourcePath.getParent();
        if (sourceDirectory == null) {
            sourceDirectory = Paths.get(".").toAbsolutePath().getParent();
        }

        var datePathString = targetDate.format(DATE_FORMATTER_PATH);
        var targetDirectory = sourceDirectory.resolve(datePathString);
        return targetDirectory.resolve(sourcePath.getFileName());
    }

    @Override
    public void performBackup(Path sourcePath, Path targetPath) throws IOException {
        createTargetDirectories(targetPath);
        moveFile(sourcePath, targetPath);
    }

    private void createTargetDirectories(Path targetPath) throws IOException {
        var targetDirectory = targetPath.getParent();
        Files.createDirectories(targetDirectory);
        log.debug("Ensured target directory exists: {}", targetDirectory);
    }

    private void moveFile(Path sourcePath, Path targetPath) throws IOException {
        log.info("Attempting to back up '{}' to '{}'", sourcePath, targetPath);

        try {
            performAtomicMove(sourcePath, targetPath);
        } catch (AtomicMoveNotSupportedException e) {
            performNonAtomicMove(sourcePath, targetPath);
        } catch (FileAlreadyExistsException e) {
            handleFileExistsError(targetPath, e);
        } catch (IOException e) {
            handleMoveError(sourcePath, targetPath, e);
        }
    }

    private void performAtomicMove(Path sourcePath, Path targetPath) throws IOException {
        Files.move(sourcePath, targetPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
        log.info("Successfully backed up '{}' to '{}'", sourcePath, targetPath);
    }

    private void performNonAtomicMove(Path sourcePath, Path targetPath) throws IOException {
        log.warn("Atomic move not supported on this file system for '{}' -> '{}'. Attempting non-atomic move.",
                sourcePath, targetPath);
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully backed up '{}' to '{}' (non-atomic).", sourcePath, targetPath);
    }

    private void handleFileExistsError(Path targetPath, FileAlreadyExistsException e) throws FileAlreadyExistsException {
        log.error("Backup failed: Target file '{}' already exists (unexpected with REPLACE_EXISTING).",
                targetPath, e);
        throw e;
    }

    private void handleMoveError(Path sourcePath, Path targetPath, IOException e) throws IOException {
        log.error("Backup failed: Could not move file '{}' to '{}': {}",
                sourcePath, targetPath, e.getMessage(), e);
        throw e;
    }
}