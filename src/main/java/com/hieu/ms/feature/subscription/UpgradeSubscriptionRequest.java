package com.hieu.ms.feature.subscription;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpgradeSubscriptionRequest {
    private PlanType planType;
    private String provider;
}
