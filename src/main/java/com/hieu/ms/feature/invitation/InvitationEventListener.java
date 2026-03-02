package com.hieu.ms.feature.invitation;

import jakarta.mail.MessagingException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hieu.ms.shared.event.InvitationEvent;
import com.hieu.ms.shared.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvitationEventListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvitationEvent(InvitationEvent event) {
        log.info(
                "📩 Handling InvitationEvent for email: {}",
                event.getInvitation().getEmail());
        try {
            emailService.sendInvitationEmail(
                    event.getInvitation(), event.getProject(), event.getInviterEmail(), event.isUserExists());
        } catch (MessagingException e) {
            log.error(
                    " Failed to send invitation email to {}: {}",
                    event.getInvitation().getEmail(),
                    e.getMessage());
        }
    }
}
