package com.hieu.ms.shared.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Validated
@Getter
@Setter
public class VNPayProperties {

    @NotBlank
    private String version = "2.1.0";

    @NotBlank
    private String orderType = "other";

    @NotBlank
    private String currency = "VND";

    @NotBlank
    private String locale = "vn";

    @NotBlank
    private String defaultBankCode = "NCB";

    @NotBlank
    private String transactionTypeRefund = "02";

    @Min(1)
    private int paymentExpiryMinutes = 15;

    @NotBlank
    private String timezone = "Etc/GMT+7";

    @NotBlank
    private String dateFormat = "yyyyMMddHHmmss";
}
