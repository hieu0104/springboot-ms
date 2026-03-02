package com.hieu.ms.shared.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class PaymentSuccessEvent extends ApplicationEvent {
    private final String orderId;

    public PaymentSuccessEvent(Object source, String orderId) {
        super(source);
        this.orderId = orderId;
    }
}
