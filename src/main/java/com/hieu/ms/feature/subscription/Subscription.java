package com.hieu.ms.feature.subscription;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.user.User;

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
        indexes = {
            @Index(name = "idx_subscription_plan_type", columnList = "planType"),
            @Index(name = "idx_subscription_is_valid", columnList = "isValid"),
            @Index(name = "idx_subscription_end_date", columnList = "subscriptionEndDate")
        })
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Version
    Long version;

    LocalDate subscriptionStartDate;
    LocalDate subscriptionEndDate; // Fixed: renamed from getSubscriptionEndDate

    @Enumerated(EnumType.STRING)
    PlanType planType;

    boolean isValid;

    @JsonIgnore
    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    User user;

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}
