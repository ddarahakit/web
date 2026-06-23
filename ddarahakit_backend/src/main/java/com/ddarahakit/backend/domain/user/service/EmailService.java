package com.ddarahakit.backend.domain.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.ddarahakit.backend.common.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;

    @Value("${app.domain.server}")
    private String domain;

    @Async
    public void sendEmail(String email, String uuid, String type) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);

            String subject = "";
            String htmlContent = "";

            if (type.equals(EMAIL_TYPE_SIGNUP)) {
                subject = "[따라학IT] 가입 환영";
                htmlContent = "<h2 style='color: #2e6c80;'>가입을 환영합니다!</h2>"
                        + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>"
                        + "<a href='" + domain + "/user/email/verify?email=" + email + "&uuid=" + uuid + "'>이메일 인증하기</a>";
            } else if (type.equals(EMAIL_TYPE_PASSWORD_RESET)) {
                subject = "[따라학IT] 비밀번호 변경 링크";
                htmlContent = "<h2 style='color: #d9534f;'>비밀번호 변경 요청</h2>"
                        + "<p>아래 링크를 클릭하여 비밀번호를 변경해주세요:</p>"
                        + "<a href='" + domain + "/user/password/reset?email=" + email + "&uuid=" + uuid + "'>비밀번호 변경하기</a>";
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true); // 두 번째 파라미터가 true면 HTML 형식이라는 뜻

            emailSender.send(message);
        } catch (MessagingException e) {
            // @Async 라 호출자가 예외를 받지 못하므로 여기서 로깅한다.
            log.error("이메일 발송 실패 (type={}, to={})", type, email, e);
        }
    }
}
