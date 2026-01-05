package com.hieu.ms.shared.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.hieu.ms.feature.invitation.Invitation;
import com.hieu.ms.feature.project.Project;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender javaMailSender;
    SpringTemplateEngine templateEngine;

    @NonFinal
    @Value("${app.frontend.url:http://localhost:5173}")
    String frontendUrl;

    public void sendInvitationEmail(Invitation invitation, Project project, String inviterEmail, boolean userExists)
            throws MessagingException {

        String actionLink;
        if (userExists) {
            actionLink = frontendUrl + "/accept-invitation?token=" + invitation.getToken();
        } else {
            actionLink = String.format(
                    "%s/register?email=%s&ref=invitation&token=%s",
                    frontendUrl, invitation.getEmail(), invitation.getToken());
        }

        Context context = new Context();
        context.setVariable("email", invitation.getEmail());
        context.setVariable("projectName", project.getName());
        context.setVariable("inviterEmail", inviterEmail);
        context.setVariable("actionLink", actionLink);
        context.setVariable("expirationHours", 168); // Should ideally come from config or invitation

        String htmlContent = templateEngine.process("email/invitation", context);

        sendHtmlEmail(invitation.getEmail(), "Project Invitation: " + project.getName(), htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        try {
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MailSendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new MailSendException("Failed to send email to " + to);
        }
    }
}
