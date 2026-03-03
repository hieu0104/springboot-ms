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
                .status(IssueStatus.TODO)
                .priority("medium")
                .project(testProject)
                .build();
        testIssue.setId("issue-1");
    }

    @Nested
    @DisplayName("transitionIssue — Workflow Engine")
    class TransitionTests {

        @Test
        @DisplayName("TODO → IN_PROGRESS: valid transition should update status")
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
        @DisplayName("IN_PROGRESS → REVIEW: should not set completedAt")
        void transitionToResolved_shouldSetCompletedAt() {
            // Arrange
            testIssue.setStatus(IssueStatus.IN_PROGRESS);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.transitionIssue("issue-1", IssueStatus.REVIEW, authentication);

            // Assert
            assertThat(result.getStatus()).isEqualTo(IssueStatus.REVIEW);
            assertThat(result.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("REVIEW → IN_PROGRESS (rework): should clear completedAt")
        void reopenIssue_shouldClearCompletedAt() {
            // Arrange
            testIssue.setStatus(IssueStatus.REVIEW);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Issue result = issueService.transitionIssue("issue-1", IssueStatus.IN_PROGRESS, authentication);

            // Assert
            assertThat(result.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            assertThat(result.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("TODO → REVIEW: invalid transition should throw INVALID_TRANSITION")
        void invalidTransition_shouldThrowException() {
            // Arrange
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));

            // Act & Assert
            assertThatThrownBy(() -> issueService.transitionIssue("issue-1", IssueStatus.REVIEW, authentication))
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
        @DisplayName("Create issue with valid data should default to TODO status")
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
            assertThat(result.getStatus()).isEqualTo(IssueStatus.TODO);
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

        @Test
        @DisplayName("Update title, description, status, and tags covers all field branches")
        void updateAllProvidedFields() {
            java.util.List<String> tags = java.util.List.of("bug", "urgent");
            IssuesRequest request = IssuesRequest.builder()
                    .title("New Title")
                    .description("New Desc")
                    .status(IssueStatus.IN_PROGRESS)
                    .tags(tags)
                    .build();

            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            Issue result = issueService.updateIssue("issue-1", request);

            assertThat(result.getTitle()).isEqualTo("New Title");
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            assertThat(result.getTags()).isEqualTo(tags);
        }

        @Test
        @DisplayName("Update with assigneeId: fetches and sets assignee")
        void updateAssignee() {
            User assignee = User.builder().username("dev").build();
            assignee.setId("user-dev");

            IssuesRequest request =
                    IssuesRequest.builder().assigneeId("user-dev").build();

            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(userService.findUserById("user-dev")).thenReturn(assignee);
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            Issue result = issueService.updateIssue("issue-1", request);

            assertThat(result.getAssignee()).isEqualTo(assignee);
        }
    }

    @Nested
    @DisplayName("getIssueById")
    class GetIssueByIdTests {

        @Test
        @DisplayName("found: returns issue")
        void found_returnsIssue() {
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));

            Issue result = issueService.getIssueById("issue-1");

            assertThat(result).isEqualTo(testIssue);
        }

        @Test
        @DisplayName("not found: throws ISSUE_NOT_FOUND")
        void notFound_throwsIssueNotFound() {
            when(issueRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> issueService.getIssueById("missing"))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ISSUE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getIssueByProjectId")
    class GetIssueByProjectIdTests {

        @Test
        @DisplayName("delegates to repository and returns list")
        void delegatesToRepository() {
            when(issueRepository.findByProjectId("project-1")).thenReturn(java.util.List.of(testIssue));

            java.util.List<Issue> result = issueService.getIssueByProjectId("project-1");

            assertThat(result).hasSize(1).containsExactly(testIssue);
        }
    }

    @Nested
    @DisplayName("deleteIssue")
    class DeleteIssueTests {

        @Test
        @DisplayName("project owner: deletes successfully")
        void owner_deletesSuccessfully() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(testUser);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));

            issueService.deleteIssue("issue-1", authentication);

            verify(issueRepository).deleteById("issue-1");
        }

        @Test
        @DisplayName("project member: deletes successfully")
        void member_deletesSuccessfully() {
            User member = User.builder().username("member").build();
            member.setId("user-member");

            Project projectWithMember = new Project();
            projectWithMember.setId("project-1");
            projectWithMember.setOwner(testUser);
            projectWithMember.getTeams().add(member);

            Issue issueInProject = Issue.builder()
                    .title("Test Issue")
                    .status(IssueStatus.TODO)
                    .project(projectWithMember)
                    .build();
            issueInProject.setId("issue-1");

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(member);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issueInProject));

            issueService.deleteIssue("issue-1", authentication);

            verify(issueRepository).deleteById("issue-1");
        }

        @Test
        @DisplayName("stranger (not owner, not member): throws UNAUTHORIZED")
        void unauthorized_throwsException() {
            User stranger = User.builder().username("stranger").build();
            stranger.setId("user-stranger");

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(stranger);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));

            assertThatThrownBy(() -> issueService.deleteIssue("issue-1", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));

            verify(issueRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("addUserToIssue")
    class AddUserToIssueTests {

        @Test
        @DisplayName("assigns user to issue and saves")
        void assignsUserAndSaves() {
            User assignee = User.builder().username("dev").build();
            assignee.setId("user-dev");

            when(userService.findUserById("user-dev")).thenReturn(assignee);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(testIssue)).thenReturn(testIssue);

            Issue result = issueService.addUsertoIssue("issue-1", "user-dev");

            assertThat(result.getAssignee()).isEqualTo(assignee);
            verify(issueRepository).save(testIssue);
        }
    }

    @Nested
    @DisplayName("transitionIssue — DONE status")
    class TransitionToClosedTests {

        @Test
        @DisplayName("REVIEW → DONE: sets completedAt (approved flow)")
        void transitionToClosed_setsCompletedAt() {
            testIssue.setStatus(IssueStatus.REVIEW);
            when(issueRepository.findById("issue-1")).thenReturn(Optional.of(testIssue));
            when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

            Issue result = issueService.transitionIssue("issue-1", IssueStatus.DONE, authentication);

            assertThat(result.getStatus()).isEqualTo(IssueStatus.DONE);
            assertThat(result.getCompletedAt()).isNotNull();
        }
    }
}
