package com.Eges411Team.UnifiedPatientManager.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String myEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        if (toEmail == null || toEmail.isBlank()) {
            logger.warn("Attempted to send OTP to null/empty email");
            throw new IllegalArgumentException("Recipient email is required");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (myEmail != null && !myEmail.isBlank()) {
            message.setFrom(myEmail);
        }
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otpCode + "\nThis code will expire in 5 minutes.");

        try {
            mailSender.send(message);
            logger.info("Sent OTP email to {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw e;
        }
    }

}
