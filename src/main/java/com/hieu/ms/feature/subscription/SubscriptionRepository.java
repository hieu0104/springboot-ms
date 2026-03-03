package com.hieu.ms.feature.subscription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByUserId(String userId);

    java.util.List<Subscription> findByIsValidTrueAndSubscriptionEndDateBefore(java.time.LocalDate date);
}
