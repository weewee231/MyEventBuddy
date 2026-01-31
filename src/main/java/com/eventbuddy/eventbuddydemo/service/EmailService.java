package com.eventbuddy.eventbuddydemo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.info("Sending email to: {} with subject: {}", to, subject);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("eventbuddymaneger@gmail.com");

            javaMailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", to, e);
            throw e;
        }
    }
}