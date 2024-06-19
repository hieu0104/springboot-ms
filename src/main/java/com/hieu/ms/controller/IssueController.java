package com.hieu.ms.controller;

import com.hieu.ms.dto.request.IssuesRequest;
import com.hieu.ms.entity.Issue;
import com.hieu.ms.entity.IssueStatus;
import com.hieu.ms.service.IssueService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Slf4j
public class IssueController {

    private final IssueService issueService;

    @GetMapping("/{issueId}")
    public ResponseEntity<Issue> getIssueById(@PathVariable String issueId) {
        try {
            Issue issue = issueService.getIssueById(issueId);
            return ResponseEntity.ok(issue);
        } catch (EntityExistsException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Issue>> getIssuesByProjectId(@PathVariable String projectId) {
        List<Issue> issues = issueService.getIssueByProjectId(projectId);
        return ResponseEntity.ok(issues);
    }

    @PostMapping
    public ResponseEntity<Issue> createIssue(@RequestBody IssuesRequest request,
                                             Authentication connectedUser) {
        Issue issue = issueService.createIssue(request, connectedUser);
        return ResponseEntity.status(201).body(issue);
    }

    @DeleteMapping("/{issueId}")
    public ResponseEntity<Void> deleteIssue(@PathVariable String issueId,
                                           Authentication connectedUser) {
        issueService.deleteIssue(issueId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{issueId}/assign/{userId}")
    public ResponseEntity<Issue> addUserToIssue(@PathVariable String issueId,
                                                @PathVariable String userId) {
        Issue issue = issueService.addUsertoIssue(issueId, userId);
        return ResponseEntity.ok(issue);
    }

    @PutMapping("/{issueId}/status/{status}")
    public ResponseEntity<Issue> updateStatus(@PathVariable String issueId,
                                              @PathVariable String status) {
        Issue issue = issueService.updateStatus(issueId, status);
        return ResponseEntity.ok(issue);
    }
}
