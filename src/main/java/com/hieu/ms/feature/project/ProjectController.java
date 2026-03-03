package com.hieu.ms.feature.project;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import com.hieu.ms.shared.dto.response.ApiResponse;

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

    @PostMapping
    @Operation(summary = "Tạo dự án mới", description = "Tạo một dự án mới trong hệ thống")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @RequestBody Project project, Authentication connectedUser) {
        Project created = projectService.createProject(project, connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Project>builder().result(created).build());
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách dự án",
            description = "Lấy danh sách dự án theo nhóm, có thể lọc theo category và tag")
    public ApiResponse<List<ProjectResponse>> getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            Authentication connected) {
        return ApiResponse.<List<ProjectResponse>>builder()
                .result(projectService.getProjectResponsesByTeam(connected, category, tag))
                .build();
    }

    @PatchMapping("/{projectId}")
    @Operation(summary = "Cập nhật dự án", description = "Cập nhật thông tin dự án")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable String projectId, @RequestBody ProjectRequest request) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.updateProject(request, projectId))
                .build();
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Xóa dự án", description = "Xóa một dự án khỏi hệ thống")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId, Authentication connectedUser) {
        projectService.deleteProject(projectId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm dự án", description = "Tìm kiếm dự án theo keyword")
    public ApiResponse<List<Project>> searchProject(
            @RequestParam(required = false) String keyword, Authentication connectedUser) {
        return ApiResponse.<List<Project>>builder()
                .result(projectService.searchProjects(keyword, connectedUser))
                .build();
    }

    @GetMapping("/{projectId}/chat")
    @Operation(summary = "Lấy chat của dự án", description = "Lấy room chat của dự án")
    public ApiResponse<Chat> getChatByProjectId(@PathVariable String projectId) {
        return ApiResponse.<Chat>builder()
                .result(projectService.getChatByProjectId(projectId))
                .build();
    }

    // NOTE: Invitation endpoints moved to InvitationController
    // Use POST /invitations/send and POST /invitations/accept instead
}
