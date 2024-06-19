package com.hieu.ms.service;

import com.hieu.ms.dto.request.ProjectRequest;
import com.hieu.ms.dto.request.ProjectUpdateRequest;
import com.hieu.ms.dto.response.ProjectResponse;
import com.hieu.ms.entity.Chat;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import com.hieu.ms.exception.AppException;
import com.hieu.ms.exception.ErrorCode;
import com.hieu.ms.mapper.ProjectMapper;
import com.hieu.ms.repository.ProjectRepository;
import com.hieu.ms.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.criteria.JpaRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //
//    public ProjectResponse createProject(ProjectRequest request, Authentication connectedUser) {
//        User user = (User) connectedUser.getPrincipal();
//        Project project = projectMapper.toProject(request);
//        project.getTeams().add(user);
//        project = projectRepository.save(project);
//
//        Chat chat = new Chat();
//        chat.setProject(project);
//        Chat projectChat = chatService.createChat(chat);
//        project.setChat(projectChat);
//
//        return projectMapper.toProjectResponse(project);
//    }


    public Project createProject(Project project, Authentication connectedUser) {
//        Jwt jwt = (Jwt) connectedUser.getPrincipal();
//        String username = jwt.getClaim("sub");
//        log.info("User name: {}",username);
//
//// Lấy các thông tin khác như user_id, email, roles, permissions,...
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        project.getTeams().add(user);
        project.setOwner(user);
        log.info("user owner: {}", project.getOwner());
        project = projectRepository.save(project);

        Chat chat = new Chat();
        chat.setProject(project);
        Chat projectChat = chatService.createChat(chat);
        project.setChat(projectChat);

        return project;
    }

    public List<Project> getProjectByTeam(Authentication connectedUser,
                                          String category,
                                          String tag) {
        //get authentication user
        User user = authenticationService.getAuthenticatedUser(connectedUser);
        List<Project> projects = projectRepository.findByTeamsContainingOrOwner(user, user);
        log.info("Projects found: {}", projects.size());

        if (category != null) {
            projects = projects
                    .stream()
                    .filter(project -> project.getCategory().equals(category))
                    .collect(Collectors.toList());
            log.info("Projects after category filter: {}", projects.size());

        }
        if (tag != null) {
            projects = projects.stream().filter(
                    project -> project.getTags().contains(tag)
            ).collect(Collectors.toList());
            log.info("Projects after tag filter: {}", projects.size());

        }
        return projects;
    }

    public Project getProjectById(String projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            throw new EntityExistsException("project not found");
        }
        return project.get();
    }

    public void deleteProject(String projectId, Authentication connectedUser) {
        User user = authenticationService.getAuthenticatedUser(connectedUser);

        getProjectById(projectId);
        projectRepository.deleteById(projectId);
    }

    public ProjectResponse updateProject(ProjectRequest request, String id) {
        Project project = getProjectById(id);
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

    public List<Project> searchProjects(String keyword, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return projectRepository.findByNameContainingAndTeamsContaining(keyword, user);
    }
}
