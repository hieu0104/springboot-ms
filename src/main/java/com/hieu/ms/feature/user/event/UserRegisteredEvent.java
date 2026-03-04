package com.hieu.ms.feature.user.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String email;

    public UserRegisteredEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}
