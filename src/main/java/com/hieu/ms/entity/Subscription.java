package com.hieu.ms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    LocalDate subscriptionStartDate;
    LocalDate getSubscriptionEndDate;
    PlanType planType;
    boolean isValid;

    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    User user;
}
