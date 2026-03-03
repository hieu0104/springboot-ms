package com.hieu.ms.feature.issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Unit Test — IssueService
 *
 * Test loại: UNIT TEST (mock tất cả dependencies)
 * Framework: JUnit 5 + Mockito + AssertJ
 * Pattern: AAA (Arrange - Act - Assert)
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    IssueRepository issueRepository;

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

    private User testUser;
    private Project testProject;
    private Issue testIssue;

    @BeforeEach
    void setUp() {
        testUser =
                User.builder().username("hieu").firstName("Hieu").lastName("Le").build();
        testUser.setId("user-1");

        testProject = Project.builder().name("Test Project").owner(testUser).build();
        testProject.setId("project-1");

        testIssue = Issue.builder()
                .title("Test Issue")
                .status(IssueStatus.OPEN)
                .priority("medium")
                .project(testProject)
                .build();
        testIssue.setId("issue-1");
    }

    @Nested
    @DisplayName("transitionIssue — Workflow Engine")
    class TransitionTests {

        @Test
        @DisplayName("OPEN → IN_PROGRESS: valid transition should update status")
        void validTransition_shouldUpdateStatus() {
            // Arrange
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.transitionIssue("issue-1", IssueStatus.IN_PROGRESS, authentication);

            // Assert
            assertThat(result.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            assertThat(result.getCompletedAt()).isNull(); // Not completed yet
            verify(issueRepository).save(testIssue);
        }

        @Test
        @DisplayName("IN_PROGRESS → RESOLVED: should set completedAt")
        void transitionToResolved_shouldSetCompletedAt() {
            // Arrange
            testIssue.setStatus(IssueStatus.IN_PROGRESS);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.transitionIssue("issue-1", IssueStatus.RESOLVED, authentication);

            // Assert
            assertThat(result.getStatus()).isEqualTo(IssueStatus.RESOLVED);
            assertThat(result.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("RESOLVED → IN_PROGRESS (reopen): should clear completedAt")
        void reopenIssue_shouldClearCompletedAt() {
            // Arrange
            testIssue.setStatus(IssueStatus.RESOLVED);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.transitionIssue("issue-1", IssueStatus.IN_PROGRESS, authentication);

            // Assert
            assertThat(result.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            assertThat(result.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("OPEN → RESOLVED: invalid transition should throw INVALID_TRANSITION")
        void invalidTransition_shouldThrowException() {
            // Arrange
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));

            // Act & Assert
            assertThatThrownBy(() -> issueService.transitionIssue("issue-1", IssueStatus.RESOLVED, authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_TRANSITION);
                    });

            verify(issueRepository, never()).save(any());
        }

        @Test
        @DisplayName("Non-existent issue should throw ISSUE_NOT_FOUND")
        void nonExistentIssue_shouldThrow() {
            // Arrange
            when(issueRepository.findById("missing")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> issueService.transitionIssue("missing", IssueStatus.IN_PROGRESS, authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.ISSUE_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("createIssue")
    class CreateTests {

        @Test
        @DisplayName("Create issue with valid data should default to OPEN status")
        void createIssue_shouldDefaultToOpen() {
            // Arrange
            IssuesRequest request = IssuesRequest.builder()
                    .title("New Task")
                    .description("Task description")
                    .projectId("project-1")
                    .priority("high")
                    .build();

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(testUser);
            when(userService.findUserById("user-1")).thenReturn(testUser);
            when(projectService.getProjectById("project-1")).thenReturn(testProject);
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.createIssue(request, authentication);

            // Assert
            assertThat(result.getTitle()).isEqualTo("New Task");
            assertThat(result.getStatus()).isEqualTo(IssueStatus.OPEN);
            assertThat(result.getProject()).isEqualTo(testProject);
        }

        @Test
        @DisplayName("Create issue with assignee should set assignee")
        void createIssue_withAssignee() {
            // Arrange
            User assignee = User.builder().username("dev1").build();
            assignee.setId("user-2");

            IssuesRequest request = IssuesRequest.builder()
                    .title("Assigned Task")
                    .projectId("project-1")
                    .assigneeId("user-2")
                    .build();

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(testUser);
            when(userService.findUserById("user-1")).thenReturn(testUser);
            when(userService.findUserById("user-2")).thenReturn(assignee);
            when(projectService.getProjectById("project-1")).thenReturn(testProject);
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.createIssue(request, authentication);

            // Assert
            assertThat(result.getAssignee()).isEqualTo(assignee);
        }
    }

    @Nested
    @DisplayName("updateIssue — Partial Update")
    class UpdateTests {

        @Test
        @DisplayName("Partial update should only change provided fields")
        void partialUpdate_shouldOnlyChangeProvidedFields() {
            // Arrange
            testIssue.setDescription("Original description");
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            IssuesRequest request = IssuesRequest.builder().priority("high").build(); // Only updating priority

            // Act
            Issue result = issueService.updateIssue("issue-1", request);

            // Assert
            assertThat(result.getPriority()).isEqualTo("high");
            assertThat(result.getTitle()).isEqualTo("Test Issue"); // Unchanged
            assertThat(result.getDescription()).isEqualTo("Original description"); // Unchanged
        }
    }
}
