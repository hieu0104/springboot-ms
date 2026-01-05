package com.hieu.ms.feature.subscription;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        indexes = {
            @Index(name = "idx_subscription_audit_user_id", columnList = "userId"),
            @Index(name = "idx_subscription_audit_subscription_id", columnList = "subscriptionId"),
            @Index(name = "idx_subscription_audit_changed_at", columnList = "changedAt")
        })
public class SubscriptionAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String subscriptionId;
    String userId;
    SubscriptionChangeType changeType;
    String oldPlan;
    String newPlan;
    Instant changedAt;
    String changedBy; // username or system
    String note;
}
