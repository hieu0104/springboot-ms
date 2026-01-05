package com.hieu.ms.feature.payment.momo;

import lombok.Data;

@Data
public class MomoPaymentResponse {
    private String payUrl;
    private String requestId;
    private String orderId;
    private String errorCode;
    private String message;
}
