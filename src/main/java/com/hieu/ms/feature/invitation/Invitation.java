package com.hieu.ms.feature.invitation;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hieu.ms.shared.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "invitation",
        indexes = {
            @Index(name = "idx_token", columnList = "token"),
            @Index(name = "idx_email_project", columnList = "email,project_id"),
            @Index(name = "idx_expires_at", columnList = "expires_at")
        })
public class Invitation extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    String token;

    @Column(nullable = false)
    String email;

    @Column(name = "project_id", nullable = false)
    String projectId;

    @Column(name = "invited_by")
    String invitedBy; // User ID who sent the invitation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    LocalDateTime acceptedAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }
}
