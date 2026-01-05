package com.hieu.ms.feature.issue;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.issue.dto.IssuesRequest;

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
    public ResponseEntity<Issue> getIssueById(@PathVariable String issueId) {
        Issue issue = issueService.getIssueById(issueId);
        return ResponseEntity.ok(issue);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Lấy issues theo project", description = "Lấy tất cả issues của một dự án")
    public ResponseEntity<List<Issue>> getIssuesByProjectId(@PathVariable String projectId) {
        List<Issue> issues = issueService.getIssueByProjectId(projectId);
        return ResponseEntity.ok(issues);
    }

    @PostMapping
    @Operation(summary = "Tạo issue mới", description = "Tạo một issue/công việc mới")
    public ResponseEntity<Issue> createIssue(@RequestBody IssuesRequest request, Authentication connectedUser) {
        Issue issue = issueService.createIssue(request, connectedUser);
        return ResponseEntity.status(201).body(issue);
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> deleteIssue(@PathVariable String issueId, Authentication connectedUser) {
        issueService.deleteIssue(issueId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{issueId}/assign/{userId}")
    public ResponseEntity<Issue> addUserToIssue(@PathVariable String issueId, @PathVariable String userId) {
        Issue issue = issueService.addUsertoIssue(issueId, userId);
        return ResponseEntity.ok(issue);
    }

    @PatchMapping("/{issueId}")
    @Operation(
            summary = "Cập nhật issue (Patch)",
            description = "Cập nhật một hoặc nhiều trường của issue (title, description, priority, status, dueDate...)")
    public ResponseEntity<Issue> patchIssue(@PathVariable String issueId, @RequestBody IssuesRequest request) {
        Issue issue = issueService.updateIssue(issueId, request);
        return ResponseEntity.ok(issue);
    }
}
