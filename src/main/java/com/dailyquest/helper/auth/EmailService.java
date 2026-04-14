package com.dailyquest.helper.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String email, String code, VerificationPurpose purpose) {
        String title;
        String bodyTitle;

        switch (purpose) {
            case REGISTER -> {
                title = "[Daily Quest Helper] 회원가입 이메일 인증코드";
                bodyTitle = "회원가입 이메일 인증코드";
            }
            case FIND_USERNAME -> {
                title = "[Daily Quest Helper] 아이디 찾기 인증코드";
                bodyTitle = "아이디 찾기 인증코드";
            }
            case RESET_PASSWORD -> {
                title = "[Daily Quest Helper] 비밀번호 재설정 인증코드";
                bodyTitle = "비밀번호 재설정 인증코드";
            }
            default -> {
                title = "[Daily Quest Helper] 이메일 인증코드";
                bodyTitle = "이메일 인증코드";
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(title);
        message.setText(
                bodyTitle + "\n\n" +
                "인증코드: " + code + "\n" +
                "유효시간: 5분\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해주세요."
        );

        try {
            log.info("인증 메일 발송 시도: to={}, purpose={}", email, purpose);
            mailSender.send(message);
            log.info("인증 메일 발송 성공: to={}", email);
        } catch (MailException e) {
            log.error("인증 메일 발송 실패: to={}, purpose={}, error={}", email, purpose, e.getMessage(), e);
            throw e;
        }
    }
}