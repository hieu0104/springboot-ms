package com.hieu.ms.feature.payment;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
public class PaymentScheduler {

    PaymentRepository paymentRepository;

    @Scheduled(fixedRateString = "${app.scheduling.payment-timeout:300000}") // 5 minutes default
    @Transactional
    public void cancelTimeoutPayments() {
        log.info("Running scheduled task to cancel timeout payments...");
        Instant timeoutThreshold = Instant.now().minus(15, ChronoUnit.MINUTES);
        List<Payment> pendingPayments =
                paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, timeoutThreshold);

        if (!pendingPayments.isEmpty()) {
            for (Payment payment : pendingPayments) {
                log.info(
                        "Cancelling payment {} due to timeout (created at {})",
                        payment.getId(),
                        payment.getCreatedAt());
                payment.setStatus(PaymentStatus.FAILED);
                payment.setProcessedAt(Instant.now());
            }
            paymentRepository.saveAll(pendingPayments);
            log.info("Cancelled {} timeout payments", pendingPayments.size());
        } else {
            log.debug("No pending timeout payments found");
        }
    }
}
