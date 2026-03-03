package com.hieu.ms.feature.issue;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import jakarta.persistence.EntityExistsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.issue.dto.IssuesRequest;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class IssueServiceErrorPathTest {

    @Mock
    IssueRepository issuesRepository;

    @Mock
    UserService userService;

    @Mock
    ProjectService projectService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    Authentication authentication;

    @InjectMocks
    IssueService issueService;

    private User owner;
    private Project project;
    private Issue issue;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        owner.setId("user-owner");

        project = new Project();
        project.setId("project-1");
        project.setOwner(owner);

        issue = Issue.builder().title("Test Issue").status(IssueStatus.TODO).build();
        issue.setId("issue-1");
        issue.setProject(project);
    }

    @Test
    @DisplayName("createIssue: projectId not found -> AppException(PROJECT_NOT_FOUND)")
    void createIssue_ProjectNotFound_ThrowsAppException() {
        // Arrange
        IssuesRequest request = IssuesRequest.builder()
                .title("New issue")
                .projectId("bad-project")
                .build();

        when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
        when(userService.findUserById("user-owner")).thenReturn(owner);
        when(projectService.getProjectById("bad-project")).thenThrow(new AppException(ErrorCode.PROJECT_NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> issueService.createIssue(request, authentication))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(issuesRepository, never()).save(any());
    }

    @Test
    @DisplayName("createIssue: assigneeId not found -> EntityExistsException")
    void createIssue_AssigneeNotFound_ThrowsException() {
        // Arrange
        IssuesRequest request = IssuesRequest.builder()
                .title("New issue")
                .projectId("project-1")
                .assigneeId("bad-assignee")
                .build();

        when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
        when(userService.findUserById("user-owner")).thenReturn(owner);
        when(userService.findUserById("bad-assignee"))
                .thenThrow(new EntityExistsException("user not found with IDbad-assignee"));

        // Act & Assert
        assertThatThrownBy(() -> issueService.createIssue(request, authentication))
                .isInstanceOf(EntityExistsException.class);

        verify(issuesRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateIssue: issue not found -> AppException(ISSUE_NOT_FOUND)")
    void updateIssue_IssueNotFound_ThrowsAppException() {
        // Arrange
        when(issuesRepository.findById("missing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssue("missing", new IssuesRequest()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ISSUE_NOT_FOUND);

        verify(issuesRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateIssue: assigneeId not found -> EntityExistsException")
    void updateIssue_AssigneeNotFound_ThrowsException() {
        // Arrange
        IssuesRequest request = IssuesRequest.builder().assigneeId("bad-user").build();

        when(issuesRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(userService.findUserById("bad-user"))
                .thenThrow(new EntityExistsException("user not found with IDbad-user"));

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssue("issue-1", request))
                .isInstanceOf(EntityExistsException.class);

        verify(issuesRepository, never()).save(any());
    }

    @Test
    @DisplayName("addUsertoIssue: userId not found -> EntityExistsException")
    void addUsertoIssue_UserNotFound_ThrowsException() {
        // Arrange
        when(userService.findUserById("bad-user"))
                .thenThrow(new EntityExistsException("user not found with IDbad-user"));

        // Act & Assert
        assertThatThrownBy(() -> issueService.addUsertoIssue("issue-1", "bad-user"))
                .isInstanceOf(EntityExistsException.class);

        verify(issuesRepository, never()).findById(any());
        verify(issuesRepository, never()).save(any());
    }

    @Test
    @DisplayName("addUsertoIssue: issueId not found -> AppException(ISSUE_NOT_FOUND)")
    void addUsertoIssue_IssueNotFound_ThrowsAppException() {
        // Arrange
        User validUser = User.builder().username("dev").build();
        validUser.setId("user-dev");

        when(userService.findUserById("user-dev")).thenReturn(validUser);
        when(issuesRepository.findById("bad-issue")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> issueService.addUsertoIssue("bad-issue", "user-dev"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ISSUE_NOT_FOUND);

        verify(issuesRepository, never()).save(any());
    }
}
