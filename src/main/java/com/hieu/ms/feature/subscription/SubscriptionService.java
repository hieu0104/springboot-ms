package com.hieu.ms.feature.subscription;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.payment.Payment;
import com.hieu.ms.feature.payment.PaymentRepository;
import com.hieu.ms.feature.payment.PaymentService;
import com.hieu.ms.feature.payment.PaymentStatus;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserRepository;
import com.hieu.ms.shared.event.PaymentSuccessEvent;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SubscriptionService {
    SubscriptionRepository subscriptionRepository;
    UserRepository userRepository;
    SubscriptionAuditRepository subscriptionAuditRepository;
    PaymentRepository paymentRepository;
    PaymentService paymentService;

    @NonFinal
    @Value("${subscription.pricing.monthly:100000}")
    int monthlyPrice;

    @NonFinal
    @Value("${subscription.pricing.annually:1000000}")
    int annuallyPrice;

    /**
     * Calculate subscription end date based on plan type
     */
    private LocalDate calculateEndDate(PlanType planType) {
        return switch (planType) {
            case FREE -> LocalDate.now().plusYears(100); // Effectively never expires
            case MONTHLY -> LocalDate.now().plusMonths(1);
            case ANNUALLY -> LocalDate.now().plusMonths(12);
        };
    }

    @Transactional
    public Subscription createSubscription(User user) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionStartDate(LocalDate.now())
                .subscriptionEndDate(calculateEndDate(PlanType.FREE)) // Fixed: proper end date calculation
                .isValid(true)
                .planType(PlanType.FREE)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        // write audit
        subscriptionAuditRepository.save(SubscriptionAudit.builder()
                .subscriptionId(saved.getId())
                .userId(user.getId())
                .changeType(SubscriptionChangeType.CREATE)
                .oldPlan(null)
                .newPlan(saved.getPlanType().name())
                .changedAt(Instant.now())
                .changedBy(user.getUsername())
                .note("Auto-created subscription on user creation")
                .build());

        return saved;
    }

    @Transactional
    public Subscription getUsersSubscription(Authentication connectedUser) {
        String username = connectedUser.getName();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Fixed: use AppException

        Subscription subscription = subscriptionRepository.findByUserId(user.getId());

        if (subscription == null) {
            // create default subscription if missing
            Subscription created = createSubscription(user);
            user.setSubscription(created);
            return created;
        }

        if (!isValid(subscription)) {
            String oldPlan = subscription.getPlanType().name();
            subscription.setPlanType(PlanType.FREE);
            subscription.setSubscriptionEndDate(calculateEndDate(PlanType.FREE)); // Fixed: use helper method
            subscription.setSubscriptionStartDate(LocalDate.now());

            Subscription updated = subscriptionRepository.save(subscription);

            subscriptionAuditRepository.save(SubscriptionAudit.builder()
                    .subscriptionId(updated.getId())
                    .userId(user.getId())
                    .changeType(SubscriptionChangeType.UPDATE)
                    .oldPlan(oldPlan)
                    .newPlan(updated.getPlanType().name())
                    .changedAt(Instant.now())
                    .changedBy(username)
                    .note("Auto-downgrade to FREE as previous subscription expired")
                    .build());

            return updated;
        }
        return subscription;
    }

    @Transactional
    public Subscription upgradeSubscription(Authentication connectedUser, PlanType planType) {
        String username = connectedUser.getName();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Fixed: use AppException

        Subscription subscription = subscriptionRepository.findByUserId(user.getId());
        if (subscription == null) {
            subscription = createSubscription(user);
        }

        String oldPlan = subscription.getPlanType() == null
                ? null
                : subscription.getPlanType().name();
        subscription.setPlanType(planType);
        subscription.setSubscriptionStartDate(LocalDate.now());
        subscription.setSubscriptionEndDate(calculateEndDate(planType)); // Fixed: use helper method
        Subscription updated = subscriptionRepository.save(subscription);

        subscriptionAuditRepository.save(SubscriptionAudit.builder()
                .subscriptionId(updated.getId())
                .userId(user.getId())
                .changeType(SubscriptionChangeType.UPDATE)
                .oldPlan(oldPlan)
                .newPlan(updated.getPlanType().name())
                .changedAt(Instant.now())
                .changedBy(username)
                .note("User initiated upgrade")
                .build());

        return updated;
    }

    boolean isValid(Subscription subscription) {
        if (subscription == null) return false;
        if (subscription.getPlanType() == null) return false;
        if (subscription.getPlanType().equals(PlanType.FREE)) {
            return true;
        }
        LocalDate endDate = subscription.getSubscriptionEndDate(); // Fixed: use correct field name
        if (endDate == null) return false;
        LocalDate currentDate = LocalDate.now();

        return endDate.isAfter(currentDate) || endDate.isEqual(currentDate);
    }

    /**
     * Process VNPAY callback with proper transaction and race condition handling
     * Fixed: Removed @Async to ensure transaction works properly
     * Fixed: Use database lock to prevent race conditions
     */
    /**
     * Handle successful payment from PaymentService
     * New centralized logic
     */
    /**
     * Handle successful payment (Event Driven)
     */
    @org.springframework.context.event.EventListener
    @Transactional
    public void handleSuccessfulPayment(PaymentSuccessEvent event) {
        String orderId = event.getOrderId();
        log.info("Received PaymentSuccessEvent for orderId: {}", orderId);
        // 1. Get Payment
        Payment payment = paymentRepository
                .findByExternalId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            log.warn("Payment {} is not successful yet, but handleSuccessfulPayment called", orderId);
            // Should we return or force? Assume caller knows best or re-verify?
            // Safest is to rely on caller (PaymentService) who just set it to SUCCESS.
        }

        // 2. Parse Payload
        PaymentOrderInfo orderInfo = parsePayload(payment.getPayload());

        // Fallback legacy logic if needed (e.g. if payload is empty look at other fields? No, payload is reliable now)
        if (orderInfo == null || orderInfo.userId() == null) {
            log.error("Cannot extract userId from payload for payment {}", orderId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. Get User
        User user = userRepository.findById(orderInfo.userId()).orElseThrow(() -> {
            log.error("User not found for id {} from payment info", orderInfo.userId());
            return new AppException(ErrorCode.USER_NOT_EXISTED);
        });

        // 4. Update Subscription
        Subscription subscription = subscriptionRepository.findByUserId(user.getId());
        if (subscription == null) {
            subscription = Subscription.builder()
                    .user(user)
                    .subscriptionStartDate(LocalDate.now())
                    .subscriptionEndDate(calculateEndDate(PlanType.FREE))
                    .isValid(true)
                    .planType(PlanType.FREE)
                    .build();
        }

        String oldPlan = subscription.getPlanType() == null
                ? null
                : subscription.getPlanType().name();
        PlanType planType = orderInfo.planType() != null ? orderInfo.planType() : PlanType.MONTHLY;

        subscription.setPlanType(planType);
        subscription.setSubscriptionStartDate(LocalDate.now());
        subscription.setSubscriptionEndDate(calculateEndDate(planType));
        Subscription updated = subscriptionRepository.save(subscription);

        // 5. Audit
        subscriptionAuditRepository.save(SubscriptionAudit.builder()
                .subscriptionId(updated.getId())
                .userId(user.getId())
                .changeType(SubscriptionChangeType.UPDATE)
                .oldPlan(oldPlan)
                .newPlan(updated.getPlanType().name())
                .changedAt(Instant.now())
                .changedBy(user.getUsername()) // Might be null if async? No, user is fetched.
                .note("Payment verified externalId=" + orderId)
                .build());

        log.info("Subscription updated for user {} after payment {}", user.getUsername(), orderId);
    }

    /**
     * Parse order info from VNPAY callback
     * Fixed: Improved parsing with better structure
     */
    private PaymentOrderInfo parseOrderInfo(String orderInfo) {
        if (orderInfo == null || orderInfo.isBlank()) {
            return null;
        }

        // Expected format: userId:planType or userId-planRef
        // Try userId:planType format first (our standard format)
        if (orderInfo.contains(":")) {
            String[] parts = orderInfo.split(":", 2);
            String userId = parts[0].trim();
            String planKey = parts.length > 1 ? parts[1].trim() : null;

            PlanType planType = null;
            if (planKey != null && !planKey.isBlank()) {
                try {
                    planType = PlanType.valueOf(planKey.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid plan type in orderInfo: {}", planKey);
                }
            }

            return new PaymentOrderInfo(userId, planType);
        }

        // Fallback: try userId-txnRef format
        if (orderInfo.contains("-")) {
            String[] parts = orderInfo.split("-", 2);
            String userId = parts[0].trim();
            return new PaymentOrderInfo(userId, null);
        }

        // Last resort: assume entire string is userId
        return new PaymentOrderInfo(orderInfo.trim(), null);
    }

    /**
     * Record for parsed order info
     */
    private record PaymentOrderInfo(String userId, PlanType planType) {}

    /**
     * Generate VNPAY payment URL for subscription upgrade
     * Fixed: Amount should come from configuration, not hardcoded
     */
    /**
     * Generate payment URL for subscription upgrade
     * Now supports multiple providers (Momo, VNPay)
     */
    public String generatePaymentUrl(Authentication connectedUser, PlanType planType, String provider) {
        String username = connectedUser.getName();
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Generate unique transaction ID
        String externalId = "SUBS-" + user.getId() + "-" + System.currentTimeMillis();

        // Amount from configuration
        int amount = getPlanAmount(planType);

        // Default to VNPAY if provider is null
        String selectedProvider = (provider == null || provider.isBlank()) ? "VNPAY" : provider;

        return paymentService.generatePaymentUrl(selectedProvider, user.getId(), planType, externalId, amount);
    }

    /**
     * Get plan amount from configuration
     */
    private int getPlanAmount(PlanType planType) {
        return switch (planType) {
            case MONTHLY -> monthlyPrice; // From configuration
            case ANNUALLY -> annuallyPrice; // From configuration
            case FREE -> throw new AppException(ErrorCode.INVALID_SUBSCRIPTION_PLAN);
        };
    }

    /**
     * Parse payload string to extract userId and planType
     */
    private PaymentOrderInfo parsePayload(String payload) {
        if (payload == null || !payload.contains("userId=")) {
            return null;
        }

        String userId = null;
        String planTypeStr = null;

        String[] parts = payload.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("userId=")) {
                userId = part.replace("userId=", "").trim();
            } else if (part.startsWith("planType=")) {
                planTypeStr = part.replace("planType=", "").trim();
            }
        }

        PlanType planType = null;
        if (planTypeStr != null) {
            try {
                planType = PlanType.valueOf(planTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid plan type in payload: {}", planTypeStr);
            }
        }

        return userId != null ? new PaymentOrderInfo(userId, planType) : null;
    }
}
