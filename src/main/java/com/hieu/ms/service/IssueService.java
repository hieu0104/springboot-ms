package com.hieu.ms.service;

import com.hieu.ms.dto.request.IssuesRequest;
import com.hieu.ms.entity.*;
import com.hieu.ms.entity.Issue;
import com.hieu.ms.mapper.ProjectMapper;
import com.hieu.ms.repository.IssuesRepository;
import com.hieu.ms.repository.ProjectRepository;
import com.hieu.ms.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IssueService {
    UserService userService;
    IssuesRepository issuesRepository;
    ProjectService projectService;
    AuthenticationService authenticationService;

    public Issue getIssueById(String issueId) {
        Optional<Issue> issue = issuesRepository.findById(issueId);
        if (issue.isPresent())
            return issue.get();
        throw new EntityExistsException("No issues fond with Id" + issueId);
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

        Issue issue = Issue.
                builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .projectID(request.getProjectId())
                .priority(request.getPriority())
                .tags(request.getTags())
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .build();
        issue.setProject(project);
        return issuesRepository.save(issue);
    }

    public void deleteIssue(String issueId, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        getIssueById(issueId);
        issuesRepository.deleteById(issueId);
    }

    public Issue addUsertoIssue(String issuesId, String userId) {
        User user = userService.findUserById(userId);
        Issue issue = getIssueById(issuesId);
        issue.setAssignee(user);
        return issuesRepository.save(issue);
    }

    public Issue updateStatus(String issueId, String status) {
        Issue issue = getIssueById(issueId);
        issue.setStatus(status);
        return issuesRepository.save(issue);
    }
}
