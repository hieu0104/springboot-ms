package com.hieu.ms.feature.issue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.issue.dto.IssuesRequest;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;
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
public class IssueService {
    UserService userService;
    IssueRepository issuesRepository;
    ProjectService projectService;
    AuthenticationService authenticationService;

    public Issue getIssueById(String issueId) {
        Optional<Issue> issue = issuesRepository.findById(issueId);
        if (issue.isPresent()) return issue.get();
        throw new AppException(ErrorCode.ISSUE_NOT_FOUND);
    }

    public List<Issue> getIssueByProjectId(String projectId) {
        return issuesRepository.findByProjectId(projectId);
    }

    public Issue createIssue(IssuesRequest request, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        user = userService.findUserById(user.getId());
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userService.findUserById(request.getAssigneeId());
        }

        Project project = projectService.getProjectById(request.getProjectId());

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : IssueStatus.TODO)
                .priority(request.getPriority())
                .tags(request.getTags())
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .build();
        issue.setProject(project);
        return issuesRepository.save(issue);
    }

    @Transactional
    public void deleteIssue(String issueId, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        Issue issue = getIssueById(issueId);
        Project project = issue.getProject();
        boolean isOwner = project.getOwner().getId().equals(user.getId());
        boolean isMember = project.getTeams() != null
                && project.getTeams().stream().anyMatch(u -> u.getId().equals(user.getId()));
        if (!isOwner && !isMember) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        issuesRepository.deleteById(issueId);
    }

    public Issue addUsertoIssue(String issuesId, String userId) {
        User user = userService.findUserById(userId);
        Issue issue = getIssueById(issuesId);
        issue.setAssignee(user);
        return issuesRepository.save(issue);
    }

    @Transactional
    public Issue updateIssue(String issueId, IssuesRequest request) {
        Issue issue = getIssueById(issueId);

        if (request.getTitle() != null) {
            issue.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            issue.setDueDate(request.getDueDate());
        }
        if (request.getTags() != null) {
            issue.setTags(request.getTags());
        }
        if (request.getAssigneeId() != null) {
            User assignee = userService.findUserById(request.getAssigneeId());
            issue.setAssignee(assignee);
        }

        return issuesRepository.save(issue);
    }

    @Transactional
    public Issue transitionIssue(String issueId, IssueStatus targetStatus, Authentication connectedUser) {
        Issue issue = getIssueById(issueId);
        IssueStatus currentStatus = issue.getStatus();

        if (!IssueTransitionRule.isAllowed(currentStatus, targetStatus)) {
            log.warn("Invalid transition: {} -> {} for issue {}", currentStatus, targetStatus, issueId);
            throw new AppException(ErrorCode.INVALID_TRANSITION);
        }

        issue.setStatus(targetStatus);

        // Auto-set completedAt when done
        if (targetStatus == IssueStatus.DONE) {
            issue.setCompletedAt(LocalDateTime.now());
        } else {
            issue.setCompletedAt(null); // Clear if reopened
        }

        log.info(
                "Issue {} transitioned: {} -> {} by {}",
                issueId,
                currentStatus,
                targetStatus,
                connectedUser != null ? connectedUser.getName() : "system");

        return issuesRepository.save(issue);
    }
}
