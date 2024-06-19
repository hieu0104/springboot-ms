package com.hieu.ms.repository;

import com.hieu.ms.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription,String> {
Subscription findByUserId(String userId);

}
