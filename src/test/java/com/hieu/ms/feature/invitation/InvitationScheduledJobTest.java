package com.hieu.ms.feature.invitation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.hieu.ms.feature.authentication.AuthenticationService;
import com.hieu.ms.feature.invitation.Invitation.InvitationStatus;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class InvitationScheduledJobTest {

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

    @InjectMocks
    InvitationService invitationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "invitationExpirationHours", 168);
    }

    @Test
    @DisplayName("expireOldInvitations: has records to expire -> calls repo with current time")
    void expireOldInvitations_HasRecords_CallsRepo() {
        // Arrange
        when(invitationRepository.expireOldInvitations(any(LocalDateTime.class)))
                .thenReturn(3);

        // Act
        invitationService.expireOldInvitations();

        // Assert
        verify(invitationRepository).expireOldInvitations(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("expireOldInvitations: no records to expire -> repo still called")
    void expireOldInvitations_NoRecords_RepoStillCalled() {
        // Arrange
        when(invitationRepository.expireOldInvitations(any())).thenReturn(0);

        // Act
        invitationService.expireOldInvitations();

        // Assert
        verify(invitationRepository).expireOldInvitations(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("expireOldInvitations: passes current time to repo")
    void expireOldInvitations_PassesCurrentTime() {
        // Arrange
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(invitationRepository.expireOldInvitations(any())).thenReturn(0);

        // Act
        LocalDateTime before = LocalDateTime.now();
        invitationService.expireOldInvitations();
        LocalDateTime after = LocalDateTime.now();

        // Assert
        verify(invitationRepository).expireOldInvitations(captor.capture());
        LocalDateTime capturedTime = captor.getValue();
        assertThat(capturedTime).isBetween(before, after);
    }

    @Test
    @DisplayName("cleanupExpiredInvitations: calls repo with cutoff and status EXPIRED")
    void cleanupExpiredInvitations_CallsRepoWithCorrectParams() {
        // Arrange
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<InvitationStatus> statusCaptor = ArgumentCaptor.forClass(InvitationStatus.class);

        // Act
        LocalDateTime before = LocalDateTime.now().minusDays(30);
        invitationService.cleanupExpiredInvitations();
        LocalDateTime after = LocalDateTime.now().minusDays(30);

        // Assert
        verify(invitationRepository).deleteByExpiresAtBeforeAndStatus(timeCaptor.capture(), statusCaptor.capture());

        assertThat(timeCaptor.getValue()).isBetween(before, after);
        assertThat(statusCaptor.getValue()).isEqualTo(InvitationStatus.EXPIRED);
    }

    @Test
    @DisplayName("cleanupExpiredInvitations: repo throws -> exception propagates")
    void cleanupExpiredInvitations_RepoThrows_PropagatesException() {
        // Arrange
        doThrow(new RuntimeException("DB error"))
                .when(invitationRepository)
                .deleteByExpiresAtBeforeAndStatus(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> invitationService.cleanupExpiredInvitations())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
