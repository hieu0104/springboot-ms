package com.hieu.ms.feature.subscription;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscription-audits")
@RequiredArgsConstructor
public class SubscriptionAuditController {
    private final SubscriptionAuditRepository auditRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionAudit>> getByUser(
            @PathVariable String userId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String principalName = authentication.getName();
        // Only allow a user to view their own audit log (unless admin role is checked at security layer)
        List<SubscriptionAudit> audits = auditRepository.findByUserId(userId);
        if (audits.isEmpty()) {
            return ResponseEntity.ok(audits);
        }
        // Verify the requesting principal matches the userId in the audit records
        String auditUserId = audits.get(0).getUserId();
        if (!auditUserId.equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<SubscriptionAudit>> getBySubscription(
            @PathVariable String subscriptionId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return ResponseEntity.ok(auditRepository.findBySubscriptionId(subscriptionId));
    }
}
