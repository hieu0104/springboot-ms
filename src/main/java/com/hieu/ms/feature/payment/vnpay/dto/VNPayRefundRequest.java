package com.hieu.ms.feature.payment.vnpay.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for VNPay refund request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayRefundRequest {
    String transactionRef; // vnp_TxnRef - Original transaction reference
    String transactionNo; // vnp_TransactionNo - VNPay transaction number
    String transactionDate; // vnp_TransactionDate - Format: yyyyMMddHHmmss
    long amount; // Amount to refund (in VND, will be multiplied by 100)
    String orderInfo; // Optional: Refund reason/description
}
