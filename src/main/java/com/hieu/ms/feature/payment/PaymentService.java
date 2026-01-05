package com.hieu.ms.feature.payment;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.subscription.PlanType;
import com.hieu.ms.shared.event.PaymentSuccessEvent;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentProvider> paymentProviders;

    @Value("${payment.vnpay.callback-url}")
    private String vnpayCallbackUrl;

    @Value("${payment.momo.return-url}")
    private String momoReturnUrl;

    @Value("${payment.momo.callback-url}")
    private String momoCallbackUrl;

    /**
     * Build payload string from userId and planType
     */
    private String buildPayload(String userId, PlanType planType) {
        return "userId=" + userId + ",planType=" + planType.name();
    }

    /**
     * Generate payment URL for any provider
     * Refactored: Delegate request building to provider (Factory Pattern)
     */
    @Transactional
    public String generatePaymentUrl(String provider, String userId, PlanType planType, String externalId, int amount) {
        // Validate input
        if (provider == null || provider.isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_NOT_FOUND);
        }
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (planType == null) {
            throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
        }
        if (externalId == null || externalId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (amount <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // Get provider from map
        PaymentProvider paymentProvider = paymentProviders.get(provider.toLowerCase());
        if (paymentProvider == null) {
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_NOT_FOUND);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .externalId(externalId)
                .status(PaymentStatus.PENDING)
                .provider(provider.toUpperCase())
                .createdAt(Instant.now())
                .payload(buildPayload(userId, planType))
                .build();
        paymentRepository.save(payment);

        // Provider builds its own request (Factory Pattern)
        Object paymentRequest = paymentProvider.buildPaymentRequest(userId, planType, externalId, amount);

        // Generate payment URL using provider
        return paymentProvider.createPayment(paymentRequest);
    }

    /**
     * Process callback from payment gateway (Standardized flow)
     */
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Process callback from payment gateway (Standardized flow)
     */
    @Transactional
    public boolean processCallback(String provider, Object callbackData) {
        PaymentProvider paymentProvider = paymentProviders.get(provider.toLowerCase());
        if (paymentProvider == null) {
            log.error("Payment provider not found: {}", provider);
            return false;
        }

        try {
            // 1. Verify and obtain transaction info from Provider
            PaymentTransactionInfo transactionInfo = paymentProvider.handleCallback(callbackData);

            // 2. Find and Validate Payment in DB
            Payment payment = paymentRepository
                    .findByExternalId(transactionInfo.orderId())
                    .orElseThrow(() -> {
                        log.error("Payment not found: {}", transactionInfo.orderId());
                        return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                    });

            // Idempotency check
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.info("Payment {} already successful, ignoring callback", transactionInfo.orderId());
                return true;
            }

            // 3. Update Payment Status
            log.info("Updating payment {} status to {}", transactionInfo.orderId(), transactionInfo.status());
            payment.setStatus(transactionInfo.status());
            payment.setProcessedAt(Instant.now());
            // Optionally store transactionRef from provider if field exists, e.g.
            // payment.setTransactionNo(transactionInfo.transactionRef());
            paymentRepository.save(payment);
            log.info("Payment {} saved to DB", transactionInfo.orderId());

            // 4. If success, Publish Event
            if (transactionInfo.status() == PaymentStatus.SUCCESS) {
                log.info("Payment success event published for orderId: {}", transactionInfo.orderId());
                eventPublisher.publishEvent(new PaymentSuccessEvent(this, transactionInfo.orderId()));
            }

            return transactionInfo.status() == PaymentStatus.SUCCESS;

        } catch (Exception e) {
            log.error("Error processing payment callback for provider {}", provider, e);
            return false;
        }
    }

    /**
     * Update payment status
     * Fixed: Use PaymentStatus enum
     */
    @Transactional
    public void updatePaymentStatus(String orderId, PaymentStatus status) {
        Payment payment = paymentRepository
                .findByExternalId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.setStatus(status);
        payment.setProcessedAt(Instant.now());
        paymentRepository.save(payment);
    }

    /**
     * Create payment for MOMO (legacy method for backward compatibility)
     */
    @Transactional
    public String createPayment(String provider, Object request) {
        PaymentProvider paymentProvider = paymentProviders.get(provider.toLowerCase());
        if (paymentProvider == null) {
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_NOT_FOUND);
        }
        return paymentProvider.createPayment(request);
    }

    /**
     * Handle callback for MOMO (legacy method for backward compatibility)
     */
    @Transactional
    public boolean handleCallback(String provider, Object callbackData) {
        return processCallback(provider, callbackData);
    }
}
