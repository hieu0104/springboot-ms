package com.hieu.ms.feature.project;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.project.dto.ProjectRequest;
import com.hieu.ms.feature.project.dto.ProjectResponse;
import com.hieu.ms.shared.dto.response.ApiResponse;
import com.hieu.ms.shared.dto.response.ChatResponse;

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

    // B1 + B5: accept ProjectRequest with @Valid, return ApiResponse<ProjectResponse>, 201 Created
    @PostMapping
    @Operation(summary = "Tạo dự án mới", description = "Tạo một dự án mới trong hệ thống")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request, Authentication connectedUser) {
        ProjectResponse created = projectService.createProject(request, connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProjectResponse>builder().result(created).build());
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

    // B5 + B8: add @Valid, pass connectedUser to service for ownership check
    @PatchMapping("/{projectId}")
    @Operation(summary = "Cập nhật dự án", description = "Cập nhật thông tin dự án")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable String projectId, @Valid @RequestBody ProjectRequest request, Authentication connectedUser) {
        return ApiResponse.<ProjectResponse>builder()
                .result(projectService.updateProject(request, projectId, connectedUser))
                .build();
    }

    // B2: ResponseEntity<Void> with noContent (already correct, kept consistent)
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Xóa dự án", description = "Xóa một dự án khỏi hệ thống")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId, Authentication connectedUser) {
        projectService.deleteProject(projectId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    // B3: return ApiResponse<List<ProjectResponse>> instead of raw entity list
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm dự án", description = "Tìm kiếm dự án theo keyword")
    public ApiResponse<List<ProjectResponse>> searchProject(
            @RequestParam(required = false) String keyword, Authentication connectedUser) {
        return ApiResponse.<List<ProjectResponse>>builder()
                .result(projectService.searchProjects(keyword, connectedUser))
                .build();
    }

    // B4: return ApiResponse<ChatResponse>, map Chat entity to ChatResponse manually
    @GetMapping("/{projectId}/chat")
    @Operation(summary = "Lấy chat của dự án", description = "Lấy room chat của dự án")
    public ApiResponse<ChatResponse> getChatByProjectId(@PathVariable String projectId) {
        Chat chat = projectService.getChatByProjectId(projectId);
        ChatResponse chatResponse = ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getName())
                .projectId(chat.getProject() != null ? chat.getProject().getId() : null)
                .build();
        return ApiResponse.<ChatResponse>builder().result(chatResponse).build();
    }

    // NOTE: Invitation endpoints moved to InvitationController
    // Use POST /invitations/send and POST /invitations/accept instead
}
