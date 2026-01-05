package com.hieu.ms.feature.payment.momo;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MomoPaymentRequest {
    @NotBlank
    private String notifyUrl;

    @NotBlank
    private String returnUrl;

    @NotBlank
    private String orderInfo;

    @NotBlank
    private String orderId;

    @NotBlank
    private String amount;
}
