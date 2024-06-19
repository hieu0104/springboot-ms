package com.hieu.ms.controller;

import com.hieu.ms.dto.request.*;
import com.hieu.ms.dto.response.ProjectResponse;
import com.hieu.ms.dto.response.UserResponse;
import com.hieu.ms.entity.Chat;
import com.hieu.ms.entity.Invitation;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import com.hieu.ms.service.InvitationService;
import com.hieu.ms.service.ProjectService;
import com.hieu.ms.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProjectController {
    ProjectService projectService;
    UserService userService;
    InvitationService invitationService;


    @PostMapping
    public Project createProject(@RequestBody Project project, Authentication connectedUser) {
        return projectService.createProject(project, connectedUser);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            Authentication connected
    ) {
        List<Project> projects = projectService.getProjectByTeam(connected, category, tag);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }


    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId,
            @RequestBody ProjectRequest request
    ) {
        ProjectResponse updatedProject = projectService.updateProject(request, projectId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Project> deleteProject(
            @PathVariable String projectId,
            Authentication connectedUser
    ) {
        projectService.deleteProject(projectId, connectedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProject(
            @RequestParam(required = false) String keyword,
            Authentication connectedUser
    ) {
        List<Project> projects = projectService.searchProjects(keyword, connectedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{projectId}/chat")
    public ResponseEntity<Chat> getChatByProjectId(
            @PathVariable String projectId
    ) {
        Chat chat = projectService.getChatByProjectId(projectId);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteProject(
            Authentication connectedUser,
            @RequestBody InviteRequest inviteRequest

    ) throws MessagingException {
        invitationService.sendInvitation(connectedUser, inviteRequest);
        return new ResponseEntity<>( HttpStatus.OK);
    }

    @GetMapping("/accept")
    public ResponseEntity<?> acceptInviteProject(
            Authentication connectedUser,
            @RequestParam String token
           // @RequestBody Project project
    ) throws Exception {
        Invitation invitation = invitationService.acceptInvitation(token, connectedUser);
        return new ResponseEntity<>(invitation, HttpStatus.OK);
    }

}
