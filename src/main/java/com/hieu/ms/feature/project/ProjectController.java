package com.hieu.ms.feature.project;

import java.util.List;

import jakarta.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.invitation.InvitationService;
import com.hieu.ms.feature.invitation.dto.InviteRequest;
import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import com.hieu.ms.feature.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Project Management", description = "APIs quản lý dự án")
public class ProjectController {
    ProjectService projectService;
    UserService userService;
    InvitationService invitationService;

    @PostMapping
    @Operation(summary = "Tạo dự án mới", description = "Tạo một dự án mới trong hệ thống")
    public Project createProject(@RequestBody Project project, Authentication connectedUser) {
        return projectService.createProject(project, connectedUser);
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách dự án",
            description = "Lấy danh sách dự án theo nhóm, có thể lọc theo category và tag")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            Authentication connected) {
        List<ProjectResponse> projects = projectService.getProjectResponsesByTeam(connected, category, tag);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable String projectId, @RequestBody ProjectRequest request) {
        ProjectResponse updatedProject = projectService.updateProject(request, projectId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Project> deleteProject(@PathVariable String projectId, Authentication connectedUser) {
        projectService.deleteProject(projectId, connectedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProject(
            @RequestParam(required = false) String keyword, Authentication connectedUser) {
        List<Project> projects = projectService.searchProjects(keyword, connectedUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{projectId}/chat")
    public ResponseEntity<Chat> getChatByProjectId(@PathVariable String projectId) {
        Chat chat = projectService.getChatByProjectId(projectId);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteProject(Authentication connectedUser, @RequestBody InviteRequest inviteRequest)
            throws MessagingException {
        invitationService.sendInvitation(connectedUser, inviteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/accept")
    public ResponseEntity<?> acceptInviteProject(Authentication connectedUser, @RequestParam String token
            // @RequestBody Project project
            ) throws Exception {
        invitationService.acceptInvitation(token, connectedUser);
        return new ResponseEntity<>("Invitation accepted successfully", HttpStatus.OK);
    }
}
