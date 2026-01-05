package com.hieu.ms.feature.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Subscription findByUserId(String userId);
}
