package com.hieu.ms.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VNPAYRequest {
    @NotBlank
    private String amount;
    @NotBlank
    private String orderInfo;
}
