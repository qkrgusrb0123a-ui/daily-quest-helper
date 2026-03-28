package com.dailyquest.helper.auth;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

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
        message.setTo(email);
        message.setSubject(title);
        message.setText(
                bodyTitle + "\n\n" +
                "인증코드: " + code + "\n" +
                "유효시간: 5분\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해주세요."
        );

        mailSender.send(message);
    }
}