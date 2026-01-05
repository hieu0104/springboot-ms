package com.hieu.ms.feature.invitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<Invitation, String> {
    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findByEmail(String userEmail);

    // Check if pending invitation exists for email + project
    boolean existsByEmailAndProjectIdAndStatus(String email, String projectId, Invitation.InvitationStatus status);

    // Find all pending invitations for a project
    List<Invitation> findByProjectIdAndStatus(String projectId, Invitation.InvitationStatus status);

    // Find all invitations by email (for user to see their invitations)
    List<Invitation> findByEmailAndStatus(String email, Invitation.InvitationStatus status);

    // Auto-expire old invitations (scheduled task)
    @Modifying
    @Query("UPDATE Invitation i SET i.status = 'EXPIRED' WHERE i.expiresAt < :now AND i.status = 'PENDING'")
    int expireOldInvitations(@Param("now") LocalDateTime now);

    // Delete expired invitations (cleanup)
    void deleteByExpiresAtBeforeAndStatus(LocalDateTime dateTime, Invitation.InvitationStatus status);
}
