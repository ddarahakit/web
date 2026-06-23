package com.ddarahakit.backend.domain.user;

import com.ddarahakit.backend.domain.user.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static com.ddarahakit.backend.common.Constants.EMAIL_TYPE_SIGNUP;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender emailSender;

    @InjectMocks
    EmailService emailService;

    @Test
    @DisplayName("메일 전송 실패(MailException)가 호출자에게 전파되지 않는다 — 가입 트랜잭션 롤백 방지")
    void sendEmail_전송실패_예외전파안함() {
        when(emailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new MailSendException("smtp down")).when(emailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() ->
                emailService.sendEmail("e2e@test.com", "uuid-123", EMAIL_TYPE_SIGNUP));

        verify(emailSender).send(any(MimeMessage.class));
    }
}
