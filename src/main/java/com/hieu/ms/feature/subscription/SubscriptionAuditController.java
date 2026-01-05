package com.hieu.ms.feature.subscription;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscription-audits")
@RequiredArgsConstructor
public class SubscriptionAuditController {
    private final SubscriptionAuditRepository auditRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionAudit>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditRepository.findByUserId(userId));
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<SubscriptionAudit>> getBySubscription(@PathVariable String subscriptionId) {
        return ResponseEntity.ok(auditRepository.findBySubscriptionId(subscriptionId));
    }
}
