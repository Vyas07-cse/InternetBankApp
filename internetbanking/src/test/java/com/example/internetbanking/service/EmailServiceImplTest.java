package com.example.internetbanking.service;

import com.example.internetbanking.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendOtpEmail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendOtpEmail("test@example.com", "123456"));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmail_Failure() {
        doThrow(new MailException("SMTP error"){}).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailService.sendOtpEmail("test@example.com", "123456"));

        assertEquals("Failed to send OTP email.", ex.getMessage());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
