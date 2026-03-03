package com.hieu.ms.feature.issue;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.issue.dto.IssuesRequest;
import com.hieu.ms.feature.issue.dto.TransitionRequest;
import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Issue Management", description = "APIs quản lý vấn đề/công việc")
public class IssueController {

    private final IssueService issueService;

    @GetMapping("/{issueId}")
    @Operation(summary = "Lấy thông tin issue", description = "Lấy thông tin chi tiết của một issue theo ID")
    public ApiResponse<Issue> getIssueById(@PathVariable String issueId) {
        return ApiResponse.<Issue>builder()
                .result(issueService.getIssueById(issueId))
                .build();
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Lấy issues theo project", description = "Lấy tất cả issues của một dự án")
    public ApiResponse<List<Issue>> getIssuesByProjectId(@PathVariable String projectId) {
        return ApiResponse.<List<Issue>>builder()
                .result(issueService.getIssueByProjectId(projectId))
                .build();
    }

    @PostMapping
    @Operation(summary = "Tạo issue mới", description = "Tạo một issue/công việc mới")
    public ResponseEntity<ApiResponse<Issue>> createIssue(
            @RequestBody IssuesRequest request, Authentication connectedUser) {
        Issue issue = issueService.createIssue(request, connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Issue>builder().result(issue).build());
    }

    @DeleteMapping("/{issueId}")
    @Operation(summary = "Xóa issue", description = "Xóa một issue")
    public ResponseEntity<Void> deleteIssue(@PathVariable String issueId, Authentication connectedUser) {
        issueService.deleteIssue(issueId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{issueId}/assign/{userId}")
    @Operation(summary = "Gán user cho issue", description = "Gán một user vào issue")
    public ApiResponse<Issue> addUserToIssue(@PathVariable String issueId, @PathVariable String userId) {
        return ApiResponse.<Issue>builder()
                .result(issueService.addUsertoIssue(issueId, userId))
                .build();
    }

    @PatchMapping("/{issueId}")
    @Operation(
            summary = "Cập nhật issue (Patch)",
            description = "Cập nhật một hoặc nhiều trường của issue (title, description, priority, dueDate...)")
    public ApiResponse<Issue> patchIssue(@PathVariable String issueId, @RequestBody IssuesRequest request) {
        return ApiResponse.<Issue>builder()
                .result(issueService.updateIssue(issueId, request))
                .build();
    }

    @PatchMapping("/{issueId}/transition")
    @Operation(
            summary = "Chuyển trạng thái issue",
            description = "Chuyển trạng thái issue theo workflow rules (OPEN → IN_PROGRESS → RESOLVED → CLOSED)")
    public ApiResponse<Issue> transitionIssue(
            @PathVariable String issueId, @RequestBody TransitionRequest request, Authentication connectedUser) {
        return ApiResponse.<Issue>builder()
                .result(issueService.transitionIssue(issueId, request.getTargetStatus(), connectedUser))
                .build();
    }
}
