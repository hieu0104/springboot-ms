package com.hieu.ms.feature.payment;

import com.hieu.ms.feature.subscription.PlanType;

public interface PaymentProvider {
    /**
     * Build provider-specific payment request
     * Each provider knows how to build its own request object
     * @param userId User ID
     * @param planType Subscription plan type
     * @param externalId External order ID
     * @param amount Payment amount
     * @return Provider-specific request object (VNPAYRequest, MomoPaymentRequest, etc.)
     */
    Object buildPaymentRequest(String userId,PlanType planType, String externalId, int amount);

    /**
     * Create payment and return payment URL
     *
     * @param request Provider-specific request object
     * @return Payment URL or response JSON
     */
    String createPayment(Object request);

    /**
     * Verify and parse callback data from payment gateway.
     * Does NOT update database or business logic.
     * @param callbackData Raw data from controller (Map, String, or Request)
     * @return PaymentTransactionInfo containing standardized result, or throws Exception if signature invalid
     */
    PaymentTransactionInfo handleCallback(Object callbackData);

    boolean refund(Object refundData);

    Object query(Object queryData);
}
