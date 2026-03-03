package com.hieu.ms.feature.invitation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.invitation.dto.InviteRequest;
import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Invitation", description = "Invitation management APIs")
public class InvitationController {
    InvitationService invitationService;

    @Operation(summary = "Send invitation", description = "Send invitation email to user to join project")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Invitation>> sendInvitation(
            @Valid @RequestBody InviteRequest request, Authentication authentication) {
        log.info("Sending invitation to {} for project {}", request.getEmail(), request.getProjectId());
        Invitation invitation = invitationService.sendInvitation(authentication, request);
        return ResponseEntity.ok(
                ApiResponse.<Invitation>builder().result(invitation).build());
    }

    @Operation(summary = "Accept invitation", description = "Accept project invitation using token")
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(
            @RequestParam String token, Authentication authentication) {
        log.info("Accepting invitation with token: {}", token);
        invitationService.acceptInvitation(token, authentication);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .result("Invitation accepted successfully")
                .build());
    }

    @Operation(summary = "Get my pending invitations", description = "Get all pending invitations for current user")
    @GetMapping("/my-pending")
    public ResponseEntity<ApiResponse<List<Invitation>>> getMyPendingInvitations(Authentication authentication) {
        List<Invitation> invitations = invitationService.getMyPendingInvitations(authentication);
        return ResponseEntity.ok(
                ApiResponse.<List<Invitation>>builder().result(invitations).build());
    }

    @Operation(
            summary = "Get pending invitations for project",
            description = "Get all pending invitations for a project")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<Invitation>>> getPendingInvitationsByProject(
            @PathVariable String projectId) {
        List<Invitation> invitations = invitationService.getPendingInvitationsByProject(projectId);
        return ResponseEntity.ok(
                ApiResponse.<List<Invitation>>builder().result(invitations).build());
    }

    @Operation(summary = "Cancel invitation", description = "Cancel a pending invitation (owner/inviter only)")
    @DeleteMapping("/{invitationId}")
    public ResponseEntity<ApiResponse<String>> cancelInvitation(
            @PathVariable String invitationId, Authentication authentication) {
        invitationService.cancelInvitation(invitationId, authentication);
        return ResponseEntity.ok(
                ApiResponse.<String>builder().result("Invitation cancelled").build());
    }

    @Operation(summary = "Resend invitation", description = "Resend an existing invitation")
    @PostMapping("/{invitationId}/resend")
    public ResponseEntity<ApiResponse<Invitation>> resendInvitation(
            @PathVariable String invitationId, Authentication authentication) {
        log.info("Resending invitation {}", invitationId);
        Invitation invitation = invitationService.resendInvitation(invitationId, authentication);
        return ResponseEntity.ok(
                ApiResponse.<Invitation>builder().result(invitation).build());
    }
}
