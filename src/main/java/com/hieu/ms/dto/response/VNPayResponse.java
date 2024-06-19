package com.hieu.ms.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VNPayResponse{
    @NotBlank
    private String amount;
    @NotBlank
    private String orderInfor;
    @NotBlank
    private String returnUrl;
}