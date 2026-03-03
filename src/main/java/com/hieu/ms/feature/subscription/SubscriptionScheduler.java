package com.hieu.ms.feature.subscription;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SubscriptionScheduler {

    SubscriptionRepository subscriptionRepository;
    SubscriptionAuditRepository subscriptionAuditRepository;

    @Scheduled(cron = "${app.scheduling.subscription-expiry:0 0 0 * * ?}") // Midnight
    @Transactional
    public void downgradeExpiredSubscriptions() {
        log.info("Running scheduled task to check for expired subscriptions...");
        LocalDate today = LocalDate.now();
        List<Subscription> expiredSubscriptions =
                subscriptionRepository.findByIsValidTrueAndSubscriptionEndDateBefore(today);

        if (!expiredSubscriptions.isEmpty()) {
            for (Subscription sub : expiredSubscriptions) {
                if (sub.getPlanType() != PlanType.FREE) {
                    log.info(
                            "Downgrading user {} subscription from {} to FREE due to expiry",
                            sub.getUser() != null ? sub.getUser().getId() : "unknown",
                            sub.getPlanType());

                    String oldPlan = sub.getPlanType().name();
                    sub.setPlanType(PlanType.FREE);
                    sub.setSubscriptionEndDate(today.plusYears(100)); // FREE never expires
                    sub.setSubscriptionStartDate(today);

                    String userId = sub.getUser() != null ? sub.getUser().getId() : "system";

                    subscriptionAuditRepository.save(SubscriptionAudit.builder()
                            .subscriptionId(sub.getId())
                            .userId(userId)
                            .changeType(SubscriptionChangeType.UPDATE)
                            .oldPlan(oldPlan)
                            .newPlan(PlanType.FREE.name())
                            .changedAt(Instant.now())
                            .changedBy("system")
                            .note("Auto-downgrade to FREE due to subscription expiry")
                            .build());
                }
            }
            subscriptionRepository.saveAll(expiredSubscriptions);
            log.info("Downgraded {} expired subscriptions", expiredSubscriptions.size());
        } else {
            log.debug("No expired subscriptions found");
        }
    }
}
