package com.copro.connect.service;

import com.copro.connect.exception.EmailSendException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", "noreply@copro.fr");
        ReflectionTestUtils.setField(emailService, "senderName", "CoPro Connect");
    }

    @Test
    @DisplayName("sendMfaCode envoie l'email avec succès (nom long)")
    void sendMfaCode_success_longName() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendMfaCode("alice@example.com", "123456", "Alice");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendMfaCode envoie avec email court (≤2 chars local part)")
    void sendMfaCode_shortLocalEmail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendMfaCode("ab@example.com", "654321", "Bob");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendMfaCode lance EmailSendException si createMimeMessage échoue")
    void sendMfaCode_createMessageFails_throwsEmailSendException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        assertThatThrownBy(() -> emailService.sendMfaCode("user@example.com", "123456", "Alice"))
                .isInstanceOf(RuntimeException.class);
    }
}
