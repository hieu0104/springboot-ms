package com.hieu.ms.feature.invitation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.invitation.Invitation.InvitationStatus;
import com.hieu.ms.feature.invitation.dto.InviteRequest;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.shared.event.InvitationEvent;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InvitationService {
    InvitationRepository invitationRepository;
    AuthenticationService authenticationService;
    UserRepository userRepository;
    ProjectService projectService;
    ApplicationEventPublisher eventPublisher;

    @NonFinal
    @Value("${app.invitation.expiration-hours:168}") // 7 days default
    int invitationExpirationHours;

    /**
     * Send invitation - GitHub style (không yêu cầu user tồn tại)
     */
    @Transactional
    public Invitation sendInvitation(Authentication authentication, InviteRequest request) throws MessagingException {
        User inviter = authenticationService.getAuthenticatedUser(authentication);

        //  Normalize email for consistency
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        log.info(" Sending invitation to email: {} (normalized: {})", request.getEmail(), normalizedEmail);

        // 1. Validate project exists
        Project project = projectService.getProjectById(request.getProjectId());

        // 2. Check duplicate pending invitation
        if (invitationRepository.existsByEmailAndProjectIdAndStatus(
                normalizedEmail, request.getProjectId(), InvitationStatus.PENDING)) {
            throw new AppException(ErrorCode.INVITATION_ALREADY_SENT);
        }

        // 3. Check user already member (nếu user tồn tại)
        if (isUserAlreadyMember(normalizedEmail, project)) {
            throw new AppException(ErrorCode.USER_ALREADY_MEMBER);
        }

        // 4. Create invitation with normalized email
        InviteRequest normalizedRequest = InviteRequest.builder()
                .email(normalizedEmail)
                .projectId(request.getProjectId())
                .build();
        Invitation invitation = createInvitation(normalizedRequest, inviter.getId());
        invitationRepository.save(invitation);

        // 5. Publish Event (Async)
        boolean userExists = userRepository.existsByEmail(normalizedEmail);
        eventPublisher.publishEvent(new InvitationEvent(this, invitation, project, inviter.getEmail(), userExists));

        log.info("Invitation event published for {} (Project: {})", normalizedEmail, project.getId());

        return invitation;
    }

    /**
     * Accept invitation manually
     */
    @Transactional
    public void acceptInvitation(String token, Authentication authentication) {
        User user = authenticationService.getAuthenticatedUser(authentication);

        Invitation invitation = invitationRepository
                .findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        validateInvitation(invitation);

        // Verify email matches
        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new AppException(ErrorCode.INVITATION_EMAIL_MISMATCH);
        }

        acceptInvitationInternal(invitation, user);

        log.info("User {} accepted invitation {} for project {}", user.getId(), token, invitation.getProjectId());
    }

    @Transactional
    public List<Invitation> autoAcceptPendingInvitations(String email) {
        // ✅ Normalize email for consistent comparison
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        log.info("🔍 Starting auto-accept for email: {} (normalized: {})", email, normalizedEmail);

        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            log.error(" Email is null or empty, cannot auto-accept invitations");
            return new ArrayList<>();
        }

        List<Invitation> pendingInvitations =
                invitationRepository.findByEmailAndStatus(normalizedEmail, InvitationStatus.PENDING);

        log.info("📧 Found {} pending invitations for {}", pendingInvitations.size(), email);

        if (pendingInvitations.isEmpty()) {
            log.info(" No pending invitations found for email: {}", normalizedEmail);
            return new ArrayList<>();
        }

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("👤 User found: {} (ID: {})", user.getEmail(), user.getId());

        List<Invitation> acceptedInvitations = new ArrayList<>();
        int acceptedCount = 0;
        int expiredCount = 0;

        for (Invitation invitation : pendingInvitations) {
            log.info(" Processing invitation ID: {} for project: {}", invitation.getId(), invitation.getProjectId());

            if (invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
                try {
                    acceptInvitationInternal(invitation, user);
                    acceptedInvitations.add(invitation);
                    acceptedCount++;
                    log.info(
                            " Successfully accepted invitation {} for project {}",
                            invitation.getId(),
                            invitation.getProjectId());
                } catch (Exception e) {
                    log.error(
                            " Failed to auto-accept invitation {} for user {}: {}",
                            invitation.getId(),
                            email,
                            e.getMessage(),
                            e);
                }
            } else {
                invitation.setStatus(InvitationStatus.EXPIRED);
                invitationRepository.save(invitation);
                expiredCount++;
                log.info(" Expired invitation {} for project {}", invitation.getId(), invitation.getProjectId());
            }
        }

        log.info(" Auto-accepted {} invitations for user {}, {} expired", acceptedCount, email, expiredCount);
        return acceptedInvitations;
    }

    /**
     * Get pending invitations for a project
     */
    public List<Invitation> getPendingInvitationsByProject(String projectId) {
        return invitationRepository.findByProjectIdAndStatus(projectId, InvitationStatus.PENDING);
    }

    /**
     * Get pending invitations for current user
     */
    public List<Invitation> getMyPendingInvitations(Authentication authentication) {
        User user = authenticationService.getAuthenticatedUser(authentication);
        return invitationRepository.findByEmailAndStatus(user.getEmail(), InvitationStatus.PENDING);
    }

    /**
     * Cancel/delete invitation (only by project owner or inviter)
     */
    @Transactional
    public void cancelInvitation(String invitationId, Authentication authentication) {
        User user = authenticationService.getAuthenticatedUser(authentication);
        Invitation invitation = invitationRepository
                .findById(invitationId)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        // Check permission
        Project project = projectService.getProjectById(invitation.getProjectId());
        if (!project.getOwner().getId().equals(user.getId())
                && !invitation.getInvitedBy().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);

        log.info("Invitation {} cancelled by user {}", invitationId, user.getId());
    }

    /**
     * Scheduled task to auto-expire old invitations
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOldInvitations() {
        int expired = invitationRepository.expireOldInvitations(LocalDateTime.now());
        if (expired > 0) {
            log.info("Auto-expired {} invitations", expired);
        }
    }

    /**
     * Cleanup expired invitations older than 30 days
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredInvitations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        invitationRepository.deleteByExpiresAtBeforeAndStatus(cutoff, InvitationStatus.EXPIRED);
        log.info("Cleaned up expired invitations older than 30 days");
    }

    // ========== PRIVATE HELPER METHODS ==========

    private Invitation createInvitation(InviteRequest request, String inviterId) {
        return Invitation.builder()
                .email(request.getEmail())
                .token(UUID.randomUUID().toString())
                .projectId(request.getProjectId())
                .invitedBy(inviterId)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(invitationExpirationHours))
                .build();
    }

    private boolean isUserAlreadyMember(String email, Project project) {
        return project.getTeams() != null
                && project.getTeams().stream()
                        .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email));
    }

    private void acceptInvitationInternal(Invitation invitation, User user) {
        Project project = projectService.getProjectById(invitation.getProjectId());

        // Add user to project if not already
        if (!project.getTeams().contains(user)) {
            project.getTeams().add(user);
            //  SAVE project to persist the team update
            projectService.saveProject(project);
        }

        // Update invitation
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private void validateInvitation(Invitation invitation) {
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new AppException(ErrorCode.INVITATION_ALREADY_PROCESSED);
        }
    }
}
