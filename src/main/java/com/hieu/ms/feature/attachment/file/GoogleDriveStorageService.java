package com.hieu.ms.feature.attachment.file;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * GoogleDriveStorageService - skeleton implementation.
 *
 * This class is intentionally minimal: it documents where to plug Google Drive
 * client logic (using Drive API) and throws a clear exception if used before
 * implementation. It's autowired optionally by FileStorageService.
 */
@Service
public class GoogleDriveStorageService implements StorageService {

    @Override
    public String storeFile(MultipartFile file, String subfolder) {
        throw new UnsupportedOperationException(
                "Google Drive storage is not implemented yet. Configure app.storage.provider=local or implement GoogleDriveStorageService.");
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        // Optionally return a UrlResource or ByteArrayResource after fetching bytes from Drive
        throw new UnsupportedOperationException("Google Drive storage is not implemented yet.");
    }

    @Override
    public boolean deleteFile(String filePath) {
        throw new UnsupportedOperationException("Google Drive storage is not implemented yet.");
    }

    @Override
    public String getStoredFilename(String filePath) {
        throw new UnsupportedOperationException("Google Drive storage is not implemented yet.");
    }

    @Override
    public long getFolderSize(String subfolder) {
        throw new UnsupportedOperationException("Google Drive storage is not implemented yet.");
    }
}
