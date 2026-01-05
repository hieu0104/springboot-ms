package com.hieu.ms.feature.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionAuditRepository extends JpaRepository<SubscriptionAudit, String> {
    List<SubscriptionAudit> findByUserId(String userId);

    List<SubscriptionAudit> findBySubscriptionId(String subscriptionId);
}
