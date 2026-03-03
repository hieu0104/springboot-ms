package com.hieu.ms.feature.attachment;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hieu.ms.feature.attachment.dto.AttachmentResponse;
import com.hieu.ms.feature.attachment.file.FileStorageService;
import com.hieu.ms.feature.issue.Issue;
import com.hieu.ms.feature.issue.IssueService;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttachmentService {

    AttachmentRepository attachmentRepository;
    FileStorageService fileStorageService;
    IssueService issueService;
    ProjectService projectService;

    private static final long MAX_STORAGE_PER_PROJECT = 500 * 1024 * 1024; // 500MB per project
    private static final int MAX_ATTACHMENTS_PER_ISSUE = 20;

    /**
     * Upload attachment cho Issue
     */
    @Transactional
    public AttachmentResponse uploadToIssue(MultipartFile file, String issueId, String description, User currentUser) {
        // Validate issue exists
        Issue issue = issueService.getIssueById(issueId);

        // Check attachment limit per issue
        long currentCount = attachmentRepository.countByIssueId(issueId);
        if (currentCount >= MAX_ATTACHMENTS_PER_ISSUE) {
            throw new AppException(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }

        // Check storage quota for project
        checkStorageQuota(issue.getProject().getId());

        // Store file
        String subfolder = "issues/" + issueId;
        String filePath = fileStorageService.storeFile(file, subfolder);
        String storedName = fileStorageService.getStoredFilename(filePath);

        // Create attachment entity
        Attachment attachment = Attachment.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .description(description)
                .issue(issue)
                .project(issue.getProject())
                .uploadedBy(currentUser)
                .build();

        attachment = attachmentRepository.save(attachment);
        log.info(
                "📎 Attachment uploaded to issue {}: {} by user {}",
                issueId,
                file.getOriginalFilename(),
                currentUser.getUsername());

        return toResponse(attachment);
    }

    /**
     * Upload attachment cho Project (không liên kết issue cụ thể)
     */
    @Transactional
    public AttachmentResponse uploadToProject(
            MultipartFile file, String projectId, String description, User currentUser) {
        Project project = projectService.getProjectById(projectId);

        // Check storage quota
        checkStorageQuota(projectId);

        // Store file
        String subfolder = "projects/" + projectId;
        String filePath = fileStorageService.storeFile(file, subfolder);
        String storedName = fileStorageService.getStoredFilename(filePath);

        Attachment attachment = Attachment.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .description(description)
                .project(project)
                .uploadedBy(currentUser)
                .build();

        attachment = attachmentRepository.save(attachment);
        log.info(
                "📎 Attachment uploaded to project {}: {} by user {}",
                projectId,
                file.getOriginalFilename(),
                currentUser.getUsername());

        return toResponse(attachment);
    }

    /**
     * Upload nhiều files cùng lúc cho Issue
     */
    @Transactional
    public List<AttachmentResponse> uploadMultipleToIssue(List<MultipartFile> files, String issueId, User currentUser) {
        // Pre-flight checks before the loop — avoid repeated fetches and quota checks per file
        Issue issue = issueService.getIssueById(issueId);
        long currentCount = attachmentRepository.countByIssueId(issueId);
        if (currentCount + files.size() > MAX_ATTACHMENTS_PER_ISSUE) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        checkStorageQuota(issue.getProject().getId());

        return files.stream()
                .map(file -> uploadToIssue(file, issueId, null, currentUser))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách attachments của Issue
     */
    public List<AttachmentResponse> getAttachmentsByIssue(String issueId) {
        return attachmentRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách attachments của Project
     */
    public List<AttachmentResponse> getAttachmentsByProject(String projectId) {
        return attachmentRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attachment by ID
     */
    public Attachment getAttachmentById(String attachmentId) {
        return attachmentRepository
                .findById(attachmentId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
    }

    /**
     * Load file để download
     */
    public Resource loadAttachmentAsResource(String attachmentId) {
        Attachment attachment = getAttachmentById(attachmentId);
        return fileStorageService.loadFileAsResource(attachment.getFilePath());
    }

    /**
     * Get original filename for download
     */
    public String getOriginalFilename(String attachmentId) {
        return getAttachmentById(attachmentId).getOriginalName();
    }

    /**
     * Get content type
     */
    public String getContentType(String attachmentId) {
        Attachment attachment = getAttachmentById(attachmentId);
        return attachment.getContentType() != null ? attachment.getContentType() : "application/octet-stream";
    }

    /**
     * Xóa attachment
     */
    @Transactional
    public void deleteAttachment(String attachmentId, User currentUser) {
        Attachment attachment = getAttachmentById(attachmentId);

        // Check permission: chỉ owner hoặc project owner mới được xóa
        boolean isOwner = attachment.getUploadedBy().getId().equals(currentUser.getId());
        boolean isProjectOwner = attachment.getProject() != null
                && attachment.getProject().getOwner().getId().equals(currentUser.getId());

        if (!isOwner && !isProjectOwner) {
            throw new AppException(ErrorCode.ATTACHMENT_ACCESS_DENIED);
        }

        // Delete physical file
        fileStorageService.deleteFile(attachment.getFilePath());

        // Delete database record
        attachmentRepository.delete(attachment);

        log.info("🗑️ Attachment deleted: {} by user {}", attachmentId, currentUser.getUsername());
    }

    /**
     * Check storage quota cho project
     */
    private void checkStorageQuota(String projectId) {
        Long currentUsage = attachmentRepository.getTotalFileSizeByProject(projectId);
        if (currentUsage != null && currentUsage >= MAX_STORAGE_PER_PROJECT) {
            throw new AppException(ErrorCode.STORAGE_QUOTA_EXCEEDED);
        }
    }

    /**
     * Lấy thống kê storage của project
     */
    public StorageStats getProjectStorageStats(String projectId) {
        Long usedBytes = attachmentRepository.getTotalFileSizeByProject(projectId);
        long attachmentCount = attachmentRepository.countByProjectId(projectId);

        return new StorageStats(usedBytes != null ? usedBytes : 0, MAX_STORAGE_PER_PROJECT, attachmentCount);
    }

    /**
     * Convert entity to response DTO
     */
    private AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .originalName(attachment.getOriginalName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .description(attachment.getDescription())
                .downloadUrl("/attachments/" + attachment.getId() + "/download")
                .issueId(attachment.getIssue() != null ? attachment.getIssue().getId() : null)
                .projectId(
                        attachment.getProject() != null
                                ? attachment.getProject().getId()
                                : null)
                .uploadedById(
                        attachment.getUploadedBy() != null
                                ? attachment.getUploadedBy().getId()
                                : null)
                .uploadedByName(
                        attachment.getUploadedBy() != null
                                ? attachment.getUploadedBy().getUsername()
                                : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    /**
     * Storage statistics record
     */
    public record StorageStats(long usedBytes, long maxBytes, long attachmentCount) {
        public double usedPercentage() {
            return maxBytes > 0 ? (double) usedBytes / maxBytes * 100 : 0;
        }

        public long remainingBytes() {
            return maxBytes - usedBytes;
        }
    }
}
