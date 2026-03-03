package com.hieu.ms.feature.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
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
import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.feature.user.UserService;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

/**
 * Unit Test — ProjectService
 *
 * Test loại: UNIT TEST (mock tất cả dependencies)
 * Framework: JUnit 5 + Mockito + AssertJ
 * Pattern: AAA (Arrange - Act - Assert)
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    ProjectRepository projectRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ChatService chatService;

    @Mock
    UserService userService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    Authentication authentication;

    @InjectMocks
    ProjectService projectService;

    private User owner;
    private User otherUser;
    private Project project;
    private ProjectRequest request;
    private ProjectResponse response;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .username("owner")
                .firstName("Owner")
                .lastName("User")
                .build();
        owner.setId("user-owner");

        otherUser = User.builder()
                .username("other")
                .firstName("Other")
                .lastName("User")
                .build();
        otherUser.setId("user-other");

        // Use new Project() so field initializers (teams=new HashSet<>(), etc.) are applied
        project = new Project();
        project.setId("project-1");
        project.setName("Test Project");
        project.setOwner(owner);

        request = ProjectRequest.builder()
                .name("Test Project")
                .description("A test project")
                .category("DEV")
                .build();

        response =
                ProjectResponse.builder().id("project-1").name("Test Project").build();
    }

    @Nested
    @DisplayName("getProjectById")
    class GetProjectByIdTests {

        @Test
        @DisplayName("found: should return the project")
        void found_shouldReturnProject() {
            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));

            Project result = projectService.getProjectById("project-1");

            assertThat(result).isEqualTo(project);
        }

        @Test
        @DisplayName("not found: should throw PROJECT_NOT_FOUND")
        void notFound_shouldThrowProjectNotFound() {
            when(projectRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.getProjectById("missing"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex ->
                            assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("deleteProject")
    class DeleteProjectTests {

        @Test
        @DisplayName("owner: should mark project as DELETED")
        void owner_shouldDeleteProject() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));

            projectService.deleteProject("project-1", authentication);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.DELETED);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("non-owner: should throw UNAUTHORIZED")
        void nonOwner_shouldThrowUnauthorized() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(otherUser);
            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectService.deleteProject("project-1", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));

            verify(projectRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("not found: should throw PROJECT_NOT_FOUND")
        void notFound_shouldThrowProjectNotFound() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.deleteProject("missing", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex ->
                            assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateProject")
    class UpdateProjectTests {

        @Test
        @DisplayName("owner: should save and return updated response")
        void owner_shouldUpdateAndReturnResponse() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toProjectResponse(project)).thenReturn(response);

            ProjectResponse result = projectService.updateProject(request, "project-1", authentication);

            assertThat(result).isEqualTo(response);
            verify(projectMapper).updateProject(request, project);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("non-owner: should throw UNAUTHORIZED")
        void nonOwner_shouldThrowUnauthorized() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(otherUser);
            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectService.updateProject(request, "project-1", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));

            verify(projectRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("searchProjects")
    class SearchProjectsTests {

        @Test
        @DisplayName("should delegate to repository and map results")
        void shouldDelegateToRepoAndMap() {
            List<Project> projects = List.of(project);
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByNameContainingAndTeamsContaining("Test", owner))
                    .thenReturn(projects);
            when(projectMapper.toProjectResponse(project)).thenReturn(response);

            List<ProjectResponse> result = projectService.searchProjects("Test", authentication);

            assertThat(result).hasSize(1).containsExactly(response);
        }

        @Test
        @DisplayName("no match: should return empty list")
        void noMatch_shouldReturnEmptyList() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByNameContainingAndTeamsContaining("nope", owner))
                    .thenReturn(List.of());

            List<ProjectResponse> result = projectService.searchProjects("nope", authentication);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createProject")
    class CreateProjectTests {

        @Test
        @DisplayName("should set owner, add to team, create chat, and return response")
        void shouldSetOwnerAddToTeamAndReturnResponse() {
            // Arrange
            Project mappedProject = new Project(); // uses field initializers so teams = new HashSet<>()
            Chat savedChat = new Chat();

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectMapper.toProject(request)).thenReturn(mappedProject);
            when(projectRepository.save(mappedProject)).thenReturn(mappedProject);
            when(chatService.createChat(any(Chat.class))).thenReturn(savedChat);
            when(projectMapper.toProjectResponse(mappedProject)).thenReturn(response);

            // Act
            ProjectResponse result = projectService.createProject(request, authentication);

            // Assert
            assertThat(result).isEqualTo(response);
            assertThat(mappedProject.getOwner()).isEqualTo(owner);
            assertThat(mappedProject.getTeams()).contains(owner);
            verify(chatService).createChat(any(Chat.class));
            verify(projectRepository).save(mappedProject);
        }
    }

    @Nested
    @DisplayName("getProjectByTeam")
    class GetProjectByTeamTests {

        @Test
        @DisplayName("category filter only: uses optimized DB query")
        void categoryOnly_usesOptimizedQuery() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByTeamMemberOrOwnerAndCategory(owner, "DEV"))
                    .thenReturn(List.of(project));

            List<Project> result = projectService.getProjectByTeam(authentication, "DEV", null);

            assertThat(result).hasSize(1).containsExactly(project);
            verify(projectRepository).findByTeamMemberOrOwnerAndCategory(owner, "DEV");
        }

        @Test
        @DisplayName("no filters: returns all projects for user")
        void noFilters_returnsAll() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByTeamsContainingOrOwner(owner, owner)).thenReturn(List.of(project));

            List<Project> result = projectService.getProjectByTeam(authentication, null, null);

            assertThat(result).hasSize(1).containsExactly(project);
        }

        @Test
        @DisplayName("tag filter: filters in-memory by tag")
        void tagFilter_filtersInMemory() {
            Project taggedProject = new Project();
            taggedProject.setId("project-tagged");
            taggedProject.setTags(new java.util.ArrayList<>(List.of("backend")));

            Project untaggedProject = new Project();
            untaggedProject.setId("project-untagged");
            untaggedProject.setTags(new java.util.ArrayList<>());

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByTeamsContainingOrOwner(owner, owner))
                    .thenReturn(List.of(taggedProject, untaggedProject));

            List<Project> result = projectService.getProjectByTeam(authentication, null, "backend");

            assertThat(result).hasSize(1).containsExactly(taggedProject);
        }

        @Test
        @DisplayName("category + tag: DB query then in-memory tag filter")
        void categoryAndTag_dbQueryThenTagFilter() {
            Project taggedProject = new Project();
            taggedProject.setId("project-tagged");
            taggedProject.setCategory("DEV");
            taggedProject.setTags(new java.util.ArrayList<>(List.of("frontend")));

            Project untaggedProject = new Project();
            untaggedProject.setId("project-untagged");
            untaggedProject.setCategory("DEV");
            untaggedProject.setTags(new java.util.ArrayList<>());

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByTeamsContainingOrOwner(owner, owner))
                    .thenReturn(List.of(taggedProject, untaggedProject));

            List<Project> result = projectService.getProjectByTeam(authentication, "DEV", "frontend");

            assertThat(result).hasSize(1).containsExactly(taggedProject);
        }
    }

    @Nested
    @DisplayName("getProjectResponsesByTeam")
    class GetProjectResponsesByTeamTests {

        @Test
        @DisplayName("maps projects to responses")
        void mapsToResponses() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(owner);
            when(projectRepository.findByTeamsContainingOrOwner(owner, owner)).thenReturn(List.of(project));
            when(projectMapper.toProjectResponse(project)).thenReturn(response);

            List<ProjectResponse> result = projectService.getProjectResponsesByTeam(authentication, null, null);

            assertThat(result).hasSize(1).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("getChatByProjectId")
    class GetChatByProjectIdTests {

        @Test
        @DisplayName("returns chat of the project")
        void returnsProjectChat() {
            Chat chat = new Chat();
            chat.setId("chat-1");
            project.setChat(chat);

            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));

            Chat result = projectService.getChatByProjectId("project-1");

            assertThat(result).isEqualTo(chat);
        }
    }

    @Nested
    @DisplayName("addUserToProject / removeUserFromProject")
    class MemberManagementTests {

        @Test
        @DisplayName("addUserToProject: user not in team → adds to team and chat, saves")
        void addUser_notInTeam_addsAndSaves() {
            Chat chat = new Chat();
            chat.setId("chat-1");
            project.setChat(chat);

            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
            when(userService.findUserById("user-other")).thenReturn(otherUser);
            when(projectRepository.save(project)).thenReturn(project);

            projectService.addUserToProject("project-1", "user-other");

            assertThat(project.getTeams()).contains(otherUser);
            assertThat(chat.getUsers()).contains(otherUser);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("addUserToProject: user already in team → skips add, still saves")
        void addUser_alreadyInTeam_skipsAdd() {
            Chat chat = new Chat();
            project.setChat(chat);
            project.getTeams().add(otherUser);

            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
            when(userService.findUserById("user-other")).thenReturn(otherUser);
            when(projectRepository.save(project)).thenReturn(project);

            projectService.addUserToProject("project-1", "user-other");

            // Teams still has exactly one otherUser (not duplicated)
            assertThat(project.getTeams()).containsOnlyOnce(otherUser);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("removeUserFromProject: user in team → removes from team and chat, saves")
        void removeUser_inTeam_removesAndSaves() {
            Chat chat = new Chat();
            chat.getUsers().add(otherUser);
            project.setChat(chat);
            project.getTeams().add(otherUser);

            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
            when(userService.findUserById("user-other")).thenReturn(otherUser);
            when(projectRepository.save(project)).thenReturn(project);

            projectService.removeUserFromProject("project-1", "user-other");

            assertThat(project.getTeams()).doesNotContain(otherUser);
            assertThat(chat.getUsers()).doesNotContain(otherUser);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("removeUserFromProject: user not in team → skips remove, still saves")
        void removeUser_notInTeam_skipsRemove() {
            Chat chat = new Chat();
            project.setChat(chat);

            when(projectRepository.findById("project-1")).thenReturn(Optional.of(project));
            when(userService.findUserById("user-other")).thenReturn(otherUser);
            when(projectRepository.save(project)).thenReturn(project);

            projectService.removeUserFromProject("project-1", "user-other");

            assertThat(project.getTeams()).doesNotContain(otherUser);
            verify(projectRepository).save(project);
        }
    }

    @Nested
    @DisplayName("saveProject")
    class SaveProjectTests {

        @Test
        @DisplayName("delegates to repository and returns saved project")
        void delegatesToRepository() {
            when(projectRepository.save(project)).thenReturn(project);

            Project result = projectService.saveProject(project);

            assertThat(result).isEqualTo(project);
            verify(projectRepository).save(project);
        }
    }
}
