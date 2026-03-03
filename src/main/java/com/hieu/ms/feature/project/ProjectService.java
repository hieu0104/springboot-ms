package com.hieu.ms.feature.project;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
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
public class ProjectService {
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ChatService chatService;
    UserService userService;
    AuthenticationService authenticationService;

    // B1 + B6: accept ProjectRequest, add @Transactional, return ProjectResponse
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        Project project = projectMapper.toProject(request);
        project.getTeams().add(user);
        project.setOwner(user);
        log.info("user owner: {}", project.getOwner());
        project = projectRepository.save(project);

        Chat chat = new Chat();
        chat.setProject(project);
        Chat projectChat = chatService.createChat(chat);
        project.setChat(projectChat);

        return projectMapper.toProjectResponse(project);
    }

    public List<Project> getProjectByTeam(Authentication connectedUser, String category, String tag) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);

        // B10: push category filtering to DB query when no tag filter needed
        List<Project> projects;
        if (category != null && tag == null) {
            projects = projectRepository.findByTeamMemberOrOwnerAndCategory(user, category);
        } else {
            projects = projectRepository.findByTeamsContainingOrOwner(user, user);
            if (category != null) {
                projects = projects.stream()
                        .filter(project -> project.getCategory().equals(category))
                        .collect(Collectors.toList());
                log.info("Projects after category filter: {}", projects.size());
            }
        }
        log.info("Projects found: {}", projects.size());

        if (tag != null) {
            projects = projects.stream()
                    .filter(project -> project.getTags().contains(tag))
                    .collect(Collectors.toList());
            log.info("Projects after tag filter: {}", projects.size());
        }
        return projects;
    }

    public List<ProjectResponse> getProjectResponsesByTeam(Authentication connectedUser, String category, String tag) {
        List<Project> projects = getProjectByTeam(connectedUser, category, tag);
        return projects.stream().map(projectMapper::toProjectResponse).collect(Collectors.toList());
    }

    public Project getProjectById(String projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project.get();
    }

    // B7: add ownership check before delete
    public void deleteProject(String projectId, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        Project project = getProjectById(projectId);
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        projectRepository.deleteById(projectId);
    }

    // B8: add Authentication param + ownership check
    public ProjectResponse updateProject(ProjectRequest request, String id, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        Project project = getProjectById(id);
        if (!project.getOwner().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        projectMapper.updateProject(request, project);
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    public void addUserToProject(String projectId, String userId) {
        Project project = getProjectById(projectId);
        User user = userService.findUserById(userId);
        if (!project.getTeams().contains(user)) {
            project.getChat().getUsers().add(user);
            project.getTeams().add(user);
        }
        projectRepository.save(project);
    }

    public void removeUserFromProject(String projectId, String userId) {
        Project project = getProjectById(projectId);
        User user = userService.findUserById(userId);
        if (project.getTeams().contains(user)) {
            project.getChat().getUsers().remove(user);
            project.getTeams().remove(user);
        }
        projectRepository.save(project);
    }

    public Chat getChatByProjectId(String projectId) {
        Project project = getProjectById(projectId);
        log.info("Project: {}", project);
        return project.getChat();
    }

    // B9: replace raw cast with authenticationService.getAuthenticatedUser
    public List<ProjectResponse> searchProjects(String keyword, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        List<Project> projects = projectRepository.findByNameContainingAndTeamsContaining(keyword, user);
        return projects.stream().map(projectMapper::toProjectResponse).collect(Collectors.toList());
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }
}
