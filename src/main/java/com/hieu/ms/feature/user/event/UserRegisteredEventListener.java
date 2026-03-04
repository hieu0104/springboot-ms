package com.hieu.ms.feature.user.event;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.hieu.ms.feature.invitation.Invitation;
import com.hieu.ms.feature.invitation.InvitationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredEventListener {

    private final InvitationService invitationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("🎯 [EVENT] UserRegisteredEventListener triggered AFTER_COMMIT for email: {}", event.getEmail());

            List<Invitation> acceptedInvitations = invitationService.autoAcceptPendingInvitations(event.getEmail());
            log.info(
                    "🎉 [EVENT] Successfully processed {} invitations for: {}",
                    acceptedInvitations.size(),
                    event.getEmail());
        } catch (Exception e) {
            log.error("❌ [EVENT] Failed to auto-accept invitations for user: {}", event.getEmail(), e);
            // Don't fail the registration if invitation processing fails
        }
    }
}
