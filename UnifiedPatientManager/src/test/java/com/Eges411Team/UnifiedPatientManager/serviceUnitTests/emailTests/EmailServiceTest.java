package com.Eges411Team.UnifiedPatientManager.serviceUnitTests.emailTests;

import com.Eges411Team.UnifiedPatientManager.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendOtpEmail_nullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendOtpEmail(null, "123456"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_blankEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendOtpEmail("   ", "123456"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_withFromAddress_setsFromAndSends() {
        ReflectionTestUtils.setField(emailService, "myEmail", "from@example.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOtpEmail("to@example.com", "654321");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertEquals("from@example.com", sent.getFrom());
        assertArrayEquals(new String[]{"to@example.com"}, sent.getTo());
        assertEquals("Your OTP Code", sent.getSubject());
        assertTrue(sent.getText().contains("654321"));
    }

    @Test
    void sendOtpEmail_withoutFromAddress_sendsWithoutFrom() {
        ReflectionTestUtils.setField(emailService, "myEmail", "");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOtpEmail("to@example.com", "111222");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertNull(sent.getFrom());
        assertArrayEquals(new String[]{"to@example.com"}, sent.getTo());
        assertEquals("Your OTP Code", sent.getSubject());
        assertTrue(sent.getText().contains("111222"));
    }

    @Test
    void sendOtpEmail_nullFromAddress_sendsWithoutFrom() {
        // Leave myEmail as null (default) to exercise the null branch
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendOtpEmail("to@example.com", "333444");

        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertNull(sent.getFrom());
        assertArrayEquals(new String[]{"to@example.com"}, sent.getTo());
        assertTrue(sent.getText().contains("333444"));
    }

    @Test
    void sendOtpEmail_mailSenderThrows_propagates() {
        ReflectionTestUtils.setField(emailService, "myEmail", "from@example.com");

        doThrow(new MailSendException("SMTP error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class, () -> emailService.sendOtpEmail("to@example.com", "777888"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
