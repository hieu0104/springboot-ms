package com.hieu.ms.feature.attachment.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file, String subfolder);

    Resource loadFileAsResource(String filePath);

    boolean deleteFile(String filePath);

    String getStoredFilename(String filePath);

    long getFolderSize(String subfolder);
}
