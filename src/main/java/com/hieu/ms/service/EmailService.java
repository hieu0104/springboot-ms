package com.hieu.ms.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender javaMailSender;

    public void sendMailWithToken(String userEmail, String link) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        String subject = "Join Project Team invitation";
        String text = "Click the link to join the project team " + link;

        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(userEmail);
        try {
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", userEmail);
        } catch (MailSendException e) {
            throw new MailSendException("Failed to send email");
        }

        javaMailSender.send(mimeMessage);
    }


}
