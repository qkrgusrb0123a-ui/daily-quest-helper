package com.dailyquest.helper.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String email, String code, VerificationPurpose purpose) {
        validateMailConfig();

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
        message.setFrom(fromEmail.trim());
        message.setTo(email);
        message.setSubject(title);
        message.setText(
                bodyTitle + "\n\n" +
                "인증코드: " + code + "\n" +
                "유효시간: 5분\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해주세요."
        );

        try {
            log.info("인증 메일 발송 시도: from={}, to={}, purpose={}", fromEmail, email, purpose);
            mailSender.send(message);
            log.info("인증 메일 발송 성공: to={}", email);
        } catch (MailAuthenticationException e) {
            log.error("메일 인증 실패: from={}, to={}, error={}", fromEmail, email, e.getMessage(), e);
            throw new IllegalStateException(
                    "이메일 로그인에 실패했습니다. MAIL_USERNAME, MAIL_PASSWORD(구글 앱 비밀번호), Gmail 계정 설정을 확인해주세요."
            );
        } catch (MailSendException e) {
            log.error("메일 전송 실패: from={}, to={}, error={}", fromEmail, email, e.getMessage(), e);
            throw new IllegalStateException(
                    "이메일 전송에 실패했습니다. 발신 메일 주소, 수신 주소, Gmail 보안 설정을 확인해주세요."
            );
        } catch (MailException e) {
            log.error("인증 메일 발송 실패: from={}, to={}, purpose={}, error={}",
                    fromEmail, email, purpose, e.getMessage(), e);
            throw new IllegalStateException(
                    "이메일 발송에 실패했습니다. 메일 계정 설정이나 앱 비밀번호를 다시 확인해주세요."
            );
        }
    }

    private void validateMailConfig() {
        if (fromEmail == null || fromEmail.isBlank() || fromEmail.contains("${")) {
            throw new IllegalStateException(
                    "메일 발신 계정이 설정되지 않았습니다. 환경변수 MAIL_USERNAME을 확인해주세요."
            );
        }

        if (mailPassword == null || mailPassword.isBlank() || mailPassword.contains("${")) {
            throw new IllegalStateException(
                    "메일 앱 비밀번호가 설정되지 않았습니다. 환경변수 MAIL_PASSWORD를 확인해주세요."
            );
        }
    }
}