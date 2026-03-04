package com.hieu.ms.feature.payment.vnpay.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for VNPay query transaction request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayQueryRequest {
    String transactionRef; // vnp_TxnRef - Transaction reference to query
    String transactionDate; // vnp_TransactionDate - Format: yyyyMMddHHmmss
}
