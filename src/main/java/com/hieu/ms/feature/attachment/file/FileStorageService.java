package com.hieu.ms.feature.attachment.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * FileStorageService - default (local) storage implementation and delegator.
 *
 * This class keeps the existing local filesystem behavior. It also supports
 * switching to another storage provider (e.g. Google Drive) by setting
 * property `app.storage.provider=gdrive` and providing a bean that implements
 * StorageService (see GoogleDriveStorageService skeleton).
 */
@Service
@Slf4j
public class FileStorageService implements StorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:20971520}")
    private long maxFileSize;

    @Value("${app.storage.provider:local}")
    private String storageProvider; // local | gdrive (future)

    private Path uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
            "csv", "json", "xml", "zip", "rar", "7z", "mp4", "avi", "mov", "mp3", "wav");

    private static final List<String> BLOCKED_CONTENT_TYPES = Arrays.asList(
            "application/x-executable", "application/x-msdownload", "application/x-sh", "text/x-shellscript");

    // Optional Google Drive implementation - may be null when not present
    @Autowired(required = false)
    private GoogleDriveStorageService googleDriveStorageService;

    // When storageProvider != 'local' and googleDriveStorageService is null, we'll fail fast.

    @PostConstruct
    public void init() {
        if (isUsingLocal()) {
            try {
                uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);
                log.info("📁 Upload directory initialized: {}", uploadPath);
            } catch (IOException e) {
                log.error("❌ Could not create upload directory: {}", uploadDir, e);
                throw new RuntimeException("Could not create upload directory", e);
            }
        } else {
            log.info("📦 Storage provider set to '{}', will delegate to provider implementation", storageProvider);
            if (googleDriveStorageService == null) {
                log.warn(
                        "⚠️ Storage provider '{}' requested but GoogleDriveStorageService bean is not available. Reverting to local.",
                        storageProvider);
                storageProvider = "local";
                init(); // re-init local
            }
        }
    }

    private boolean isUsingLocal() {
        return storageProvider == null || storageProvider.isBlank() || "local".equalsIgnoreCase(storageProvider);
    }

    // --- StorageService implementation (delegates when necessary) ---

    @Override
    public String storeFile(@Nonnull MultipartFile file, String subfolder) {
        if (!isUsingLocal()) {
            return googleDriveStorageService.storeFile(file, subfolder);
        }
        return storeFileLocal(file, subfolder);
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        if (!isUsingLocal()) {
            return googleDriveStorageService.loadFileAsResource(filePath);
        }
        return loadFileAsResourceLocal(filePath);
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (!isUsingLocal()) {
            return googleDriveStorageService.deleteFile(filePath);
        }
        return deleteFileLocal(filePath);
    }

    @Override
    public String getStoredFilename(String filePath) {
        if (!isUsingLocal()) {
            return googleDriveStorageService.getStoredFilename(filePath);
        }
        return getStoredFilenameLocal(filePath);
    }

    @Override
    public long getFolderSize(String subfolder) {
        if (!isUsingLocal()) {
            return googleDriveStorageService.getFolderSize(subfolder);
        }
        return getFolderSizeLocal(subfolder);
    }

    // --- existing local implementation moved to private methods (no API change) ---

    private String storeFileLocal(@Nonnull MultipartFile file, String subfolder) {
        validateFile(file);

        String originalFilename =
                StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;

        try {
            Path targetFolder = (subfolder == null || subfolder.isBlank()) ? uploadPath : uploadPath.resolve(subfolder);

            Files.createDirectories(targetFolder);

            Path targetPath = targetFolder.resolve(storedFilename);

            if (!targetPath.getParent().equals(targetFolder)) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ File saved: {} -> {}", originalFilename, targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("❌ Failed to store file: {}", originalFilename, e);
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    private Resource loadFileAsResourceLocal(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("⚠️ File not found or not readable: {}", filePath);
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (Exception e) {
            log.error("❌ Error loading file: {}", filePath, e);
            throw new RuntimeException("Error loading file: " + filePath, e);
        }
    }

    private boolean deleteFileLocal(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("🗑️ File deleted: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("File type not allowed: " + extension);
        }

        String contentType = file.getContentType();
        if (contentType != null && BLOCKED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Content type not allowed: " + contentType);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private String getStoredFilenameLocal(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    private long getFolderSizeLocal(String subfolder) {
        try {
            Path folder = uploadPath.resolve(subfolder);
            if (!Files.exists(folder)) {
                return 0;
            }
            try (var paths = Files.walk(folder)) {
                return paths.filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0L;
                            }
                        })
                        .sum();
            }
        } catch (IOException e) {
            log.error("Error calculating folder size", e);
            return 0;
        }
    }
}
