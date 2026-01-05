package com.hieu.ms.feature.attachment;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hieu.ms.feature.attachment.dto.AttachmentResponse;
import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Attachment Management", description = "APIs quản lý tệp đính kèm")
public class AttachmentController {

    AttachmentService attachmentService;
    AuthenticationService authenticationService;

    // ==================== UPLOAD APIs ====================

    @PostMapping(value = "/issue/{issueId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file cho Issue", description = "Upload một file đính kèm vào issue cụ thể")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadToIssue(
            @PathVariable String issueId,
            @Parameter(description = "File cần upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Mô tả file") @RequestParam(value = "description", required = false)
                    String description,
            Authentication authentication) {

        User currentUser = authenticationService.getAuthenticatedUser(authentication);
        AttachmentResponse response = attachmentService.uploadToIssue(file, issueId, description, currentUser);

        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder()
                .code(200)
                .message("File uploaded successfully")
                .result(response)
                .build());
    }

    @PostMapping(value = "/issue/{issueId}/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload nhiều files cho Issue", description = "Upload nhiều files cùng lúc vào issue")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadMultipleToIssue(
            @PathVariable String issueId,
            @Parameter(description = "Danh sách files") @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        User currentUser = authenticationService.getAuthenticatedUser(authentication);
        List<AttachmentResponse> responses = attachmentService.uploadMultipleToIssue(files, issueId, currentUser);

        return ResponseEntity.ok(ApiResponse.<List<AttachmentResponse>>builder()
                .code(200)
                .message("Files uploaded successfully")
                .result(responses)
                .build());
    }

    @PostMapping(value = "/project/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload file cho Project",
            description = "Upload file đính kèm vào project (không gắn issue cụ thể)")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadToProject(
            @PathVariable String projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        User currentUser = authenticationService.getAuthenticatedUser(authentication);
        AttachmentResponse response = attachmentService.uploadToProject(file, projectId, description, currentUser);

        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder()
                .code(200)
                .message("File uploaded successfully")
                .result(response)
                .build());
    }

    // ==================== GET/LIST APIs ====================

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Lấy danh sách attachments của Issue", description = "Lấy tất cả files đính kèm của một issue")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByIssue(@PathVariable String issueId) {

        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByIssue(issueId);

        return ResponseEntity.ok(ApiResponse.<List<AttachmentResponse>>builder()
                .code(200)
                .result(attachments)
                .build());
    }

    @GetMapping("/project/{projectId}")
    @Operation(
            summary = "Lấy danh sách attachments của Project",
            description = "Lấy tất cả files đính kèm của một project")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachmentsByProject(
            @PathVariable String projectId) {

        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByProject(projectId);

        return ResponseEntity.ok(ApiResponse.<List<AttachmentResponse>>builder()
                .code(200)
                .result(attachments)
                .build());
    }

    // ==================== DOWNLOAD API ====================

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download file", description = "Tải xuống file đính kèm theo ID")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String attachmentId) {

        Resource resource = attachmentService.loadAttachmentAsResource(attachmentId);
        String filename = attachmentService.getOriginalFilename(attachmentId);
        String contentType = attachmentService.getContentType(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/{attachmentId}/view")
    @Operation(summary = "Xem file inline", description = "Xem file trực tiếp trong browser (cho images, PDFs)")
    public ResponseEntity<Resource> viewAttachment(@PathVariable String attachmentId) {

        Resource resource = attachmentService.loadAttachmentAsResource(attachmentId);
        String contentType = attachmentService.getContentType(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    // ==================== DELETE API ====================

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Xóa attachment", description = "Xóa file đính kèm (chỉ owner hoặc project owner)")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable String attachmentId, Authentication authentication) {

        User currentUser = authenticationService.getAuthenticatedUser(authentication);
        attachmentService.deleteAttachment(attachmentId, currentUser);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Attachment deleted successfully")
                .build());
    }

    // ==================== STORAGE STATS API ====================

    @GetMapping("/project/{projectId}/storage")
    @Operation(summary = "Lấy thống kê storage", description = "Lấy thông tin dung lượng đã sử dụng của project")
    public ResponseEntity<ApiResponse<AttachmentService.StorageStats>> getStorageStats(@PathVariable String projectId) {

        AttachmentService.StorageStats stats = attachmentService.getProjectStorageStats(projectId);

        return ResponseEntity.ok(ApiResponse.<AttachmentService.StorageStats>builder()
                .code(200)
                .result(stats)
                .build());
    }
}
