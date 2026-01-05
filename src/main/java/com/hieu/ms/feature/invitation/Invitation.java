package com.hieu.ms.feature.invitation;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "invitation",
        indexes = {
            @Index(name = "idx_token", columnList = "token"),
            @Index(name = "idx_email_project", columnList = "email,project_id"),
            @Index(name = "idx_expires_at", columnList = "expires_at")
        })
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

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

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

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
