package com.hieu.ms.feature.payment;

import java.math.BigDecimal;

/**
 * Standardized information returned by Payment Providers after verifying a callback.
 */
public record PaymentTransactionInfo(
        String orderId, // Internal Order ID (traceable to Payment entity)
        String transactionRef, // Provider's transaction ID
        BigDecimal amount, // Amount paid
        PaymentStatus status, // Standardized status
        String responseCode, // Raw response code from provider
        String message, // Message/Description
        String rawData // Original raw callback data (for logging/debugging)
        ) {}
