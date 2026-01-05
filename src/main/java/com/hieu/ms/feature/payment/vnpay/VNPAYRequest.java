package com.hieu.ms.feature.payment.vnpay;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VNPAYRequest {
    @NotBlank
    private String amount;

    @NotBlank
    private String orderInfo;

    private String bankCode;
    private String returnUrl;
    private String transactionRef;
}
