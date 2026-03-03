package com.hieu.ms.feature.invitation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.invitation.Invitation.InvitationStatus;
import com.hieu.ms.feature.invitation.dto.InviteRequest;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    InvitationRepository invitationRepository;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    UserRepository userRepository;

    @Mock
    ProjectService projectService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    Authentication authentication;

    @InjectMocks
    InvitationService invitationService;

    private User inviter;
    private User invitee;
    private Project project;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "invitationExpirationHours", 168);

        inviter = User.builder().username("owner").email("owner@example.com").build();
        inviter.setId("user-owner");

        invitee =
                User.builder().username("invitee").email("invitee@example.com").build();
        invitee.setId("user-invitee");

        project = new Project();
        project.setId("project-1");
        project.setOwner(inviter);
        project.setTeams(new HashSet<>());
    }

    // -------- sendInvitation --------

    @Nested
    @DisplayName("sendInvitation")
    class SendInvitationTests {

        private InviteRequest request;

        @BeforeEach
        void setUpRequest() {
            request = InviteRequest.builder()
                    .email("invitee@example.com")
                    .projectId("project-1")
                    .build();
        }

        @Test
        @DisplayName("happy path: saves invitation and publishes event")
        void happyPath_savesAndPublishesEvent() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.existsByEmailAndProjectIdAndStatus(
                            "invitee@example.com", "project-1", InvitationStatus.PENDING))
                    .thenReturn(false);
            when(userRepository.existsByEmail("invitee@example.com")).thenReturn(false);
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            Invitation result = invitationService.sendInvitation(authentication, request);

            assertThat(result.getEmail()).isEqualTo("invitee@example.com");
            assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
            verify(invitationRepository).save(any(Invitation.class));
            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("duplicate PENDING: throws INVITATION_ALREADY_SENT")
        void duplicate_throwsInvitationAlreadySent() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.existsByEmailAndProjectIdAndStatus(
                            "invitee@example.com", "project-1", InvitationStatus.PENDING))
                    .thenReturn(true);

            assertThatThrownBy(() -> invitationService.sendInvitation(authentication, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVITATION_ALREADY_SENT));

            verify(invitationRepository, never()).save(any());
        }

        @Test
        @DisplayName("already member: throws USER_ALREADY_IN_PROJECT")
        void alreadyMember_throwsUserAlreadyInProject() {
            project.getTeams().add(invitee);

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.existsByEmailAndProjectIdAndStatus(
                            "invitee@example.com", "project-1", InvitationStatus.PENDING))
                    .thenReturn(false);

            assertThatThrownBy(() -> invitationService.sendInvitation(authentication, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_ALREADY_IN_PROJECT));
        }
    }

    // -------- acceptInvitation --------

    @Nested
    @DisplayName("acceptInvitation")
    class AcceptInvitationTests {

        private Invitation validInvitation;

        @BeforeEach
        void setUpInvitation() {
            validInvitation = Invitation.builder()
                    .token("tok-123")
                    .email("invitee@example.com")
                    .projectId("project-1")
                    .invitedBy("user-owner")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            validInvitation.setId("inv-1");
        }

        @Test
        @DisplayName("happy path: sets ACCEPTED and saves")
        void happyPath_acceptsInvitation() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(invitee);
            when(invitationRepository.findByToken("tok-123")).thenReturn(Optional.of(validInvitation));
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            invitationService.acceptInvitation("tok-123", authentication);

            assertThat(validInvitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
            assertThat(validInvitation.getAcceptedAt()).isNotNull();
        }

        @Test
        @DisplayName("not found: throws INVITATION_NOT_FOUND")
        void notFound_throwsInvitationNotFound() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(invitee);
            when(invitationRepository.findByToken("bad-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.acceptInvitation("bad-token", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex ->
                            assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVITATION_NOT_FOUND));
        }

        @Test
        @DisplayName("expired: throws INVITATION_EXPIRED")
        void expired_throwsInvitationExpired() {
            validInvitation.setExpiresAt(LocalDateTime.now().minusHours(1));

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(invitee);
            when(invitationRepository.findByToken("tok-123")).thenReturn(Optional.of(validInvitation));
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() -> invitationService.acceptInvitation("tok-123", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex ->
                            assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVITATION_EXPIRED));
        }

        @Test
        @DisplayName("not PENDING: throws INVITATION_ALREADY_ACCEPTED")
        void notPending_throwsAlreadyAccepted() {
            validInvitation.setStatus(InvitationStatus.ACCEPTED);

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(invitee);
            when(invitationRepository.findByToken("tok-123")).thenReturn(Optional.of(validInvitation));

            assertThatThrownBy(() -> invitationService.acceptInvitation("tok-123", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVITATION_ALREADY_ACCEPTED));
        }

        @Test
        @DisplayName("email mismatch: throws INVITATION_EMAIL_MISMATCH")
        void emailMismatch_throwsMismatch() {
            User wrongUser =
                    User.builder().username("other").email("other@example.com").build();
            wrongUser.setId("user-other");

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(wrongUser);
            when(invitationRepository.findByToken("tok-123")).thenReturn(Optional.of(validInvitation));

            assertThatThrownBy(() -> invitationService.acceptInvitation("tok-123", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVITATION_EMAIL_MISMATCH));
        }
    }

    // -------- autoAcceptPendingInvitations --------

    @Nested
    @DisplayName("autoAcceptPendingInvitations")
    class AutoAcceptTests {

        @Test
        @DisplayName("null email: returns empty list")
        void nullEmail_returnsEmpty() {
            List<Invitation> result = invitationService.autoAcceptPendingInvitations(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("no pending invitations: returns empty list")
        void noPending_returnsEmpty() {
            when(invitationRepository.findByEmailAndStatus("invitee@example.com", InvitationStatus.PENDING))
                    .thenReturn(List.of());

            List<Invitation> result = invitationService.autoAcceptPendingInvitations("invitee@example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("valid pending invitation: auto-accepts and returns list")
        void validPending_autoAccepts() {
            Invitation inv = Invitation.builder()
                    .token("tok-auto")
                    .email("invitee@example.com")
                    .projectId("project-1")
                    .invitedBy("user-owner")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            inv.setId("inv-auto");

            when(invitationRepository.findByEmailAndStatus("invitee@example.com", InvitationStatus.PENDING))
                    .thenReturn(List.of(inv));
            when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

            List<Invitation> result = invitationService.autoAcceptPendingInvitations("invitee@example.com");

            assertThat(result).hasSize(1);
            assertThat(inv.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        }
    }

    // -------- cancelInvitation --------

    @Nested
    @DisplayName("cancelInvitation")
    class CancelInvitationTests {

        private Invitation invitation;

        @BeforeEach
        void setUpInvitation() {
            invitation = Invitation.builder()
                    .token("tok-cancel")
                    .email("invitee@example.com")
                    .projectId("project-1")
                    .invitedBy("user-owner")
                    .status(InvitationStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            invitation.setId("inv-cancel");
        }

        @Test
        @DisplayName("project owner cancels: sets CANCELLED and saves")
        void owner_cancels() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
            when(invitationRepository.findById("inv-cancel")).thenReturn(Optional.of(invitation));
            when(projectService.getProjectById("project-1")).thenReturn(project);
            when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

            invitationService.cancelInvitation("inv-cancel", authentication);

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
            verify(invitationRepository).save(invitation);
        }

        @Test
        @DisplayName("non-owner/non-inviter: throws UNAUTHORIZED")
        void nonOwner_throwsUnauthorized() {
            User stranger = User.builder()
                    .username("stranger")
                    .email("stranger@example.com")
                    .build();
            stranger.setId("user-stranger");

            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(stranger);
            when(invitationRepository.findById("inv-cancel")).thenReturn(Optional.of(invitation));
            when(projectService.getProjectById("project-1")).thenReturn(project);

            assertThatThrownBy(() -> invitationService.cancelInvitation("inv-cancel", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(
                            ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
        }

        @Test
        @DisplayName("not found: throws INVITATION_NOT_FOUND")
        void notFound_throwsInvitationNotFound() {
            when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
            when(invitationRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.cancelInvitation("missing", authentication))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex ->
                            assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVITATION_NOT_FOUND));
        }
    }

    // -------- getPendingInvitationsByProject --------

    @Test
    @DisplayName("getPendingInvitationsByProject: delegates to repo")
    void getPendingByProject_delegatesToRepo() {
        Invitation inv = Invitation.builder()
                .projectId("project-1")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .email("a@b.com")
                .token("t")
                .invitedBy("u")
                .build();
        when(invitationRepository.findByProjectIdAndStatus("project-1", InvitationStatus.PENDING))
                .thenReturn(List.of(inv));

        List<Invitation> result = invitationService.getPendingInvitationsByProject("project-1");

        assertThat(result).hasSize(1);
    }

    // -------- getMyPendingInvitations --------

    @Test
    @DisplayName("getMyPendingInvitations: gets auth user email and delegates to repo")
    void getMyPendingInvitations_delegatesToRepo() {
        Invitation inv = Invitation.builder()
                .email("owner@example.com")
                .status(InvitationStatus.PENDING)
                .projectId("project-1")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .token("t")
                .invitedBy("u")
                .build();
        when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviter);
        when(invitationRepository.findByEmailAndStatus("owner@example.com", InvitationStatus.PENDING))
                .thenReturn(List.of(inv));

        List<Invitation> result = invitationService.getMyPendingInvitations(authentication);

        assertThat(result).hasSize(1);
    }

    // -------- autoAcceptPendingInvitations — expired path --------

    @Test
    @DisplayName("autoAcceptPendingInvitations: expired invitation gets marked EXPIRED")
    void autoAccept_expiredInvitation_markedExpired() {
        Invitation expired = Invitation.builder()
                .token("tok-expired")
                .email("invitee@example.com")
                .projectId("project-1")
                .invitedBy("user-owner")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        expired.setId("inv-expired");

        when(invitationRepository.findByEmailAndStatus("invitee@example.com", InvitationStatus.PENDING))
                .thenReturn(List.of(expired));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        List<Invitation> result = invitationService.autoAcceptPendingInvitations("invitee@example.com");

        assertThat(result).isEmpty();
        assertThat(expired.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
    }

    // -------- cancelInvitation — inviter (not owner) can cancel --------

    @Test
    @DisplayName("cancelInvitation: inviter (non-owner) can cancel their own invitation")
    void inviter_canCancelOwnInvitation() {
        // inviterUser is different from project owner
        User inviterUser =
                User.builder().username("inviter").email("inviter@example.com").build();
        inviterUser.setId("user-inviter");

        Invitation invitation = Invitation.builder()
                .token("tok")
                .email("someone@example.com")
                .projectId("project-1")
                .invitedBy("user-inviter") // same as inviterUser.getId()
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        invitation.setId("inv-by-inviter");

        when(authenticationService.getAuthenticatedUser(authentication)).thenReturn(inviterUser);
        when(invitationRepository.findById("inv-by-inviter")).thenReturn(Optional.of(invitation));
        when(projectService.getProjectById("project-1")).thenReturn(project);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(i -> i.getArgument(0));

        invitationService.cancelInvitation("inv-by-inviter", authentication);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }
}
