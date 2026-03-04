package com.hieu.ms.feature.invitation.event;

import org.springframework.context.ApplicationEvent;

import com.hieu.ms.feature.invitation.Invitation;
import com.hieu.ms.feature.project.Project;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvitationEvent extends ApplicationEvent {
    Invitation invitation;
    Project project;
    String inviterEmail;
    boolean userExists;

    public InvitationEvent(
            Object source, Invitation invitation, Project project, String inviterEmail, boolean userExists) {
        super(source);
        this.invitation = invitation;
        this.project = project;
        this.inviterEmail = inviterEmail;
        this.userExists = userExists;
    }
}
