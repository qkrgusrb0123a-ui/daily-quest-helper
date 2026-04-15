package com.dailyquest.helper.auth;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final Gmail gmail;

    @Value("${gmail.sender.email}")
    private String senderEmail;

    @Value("${gmail.sender.name:Daily Quest Helper}")
    private String senderName;

    public EmailService(Gmail gmail) {
        this.gmail = gmail;
    }

    public void sendVerificationCode(String toEmail, String code, VerificationPurpose purpose) {
        String subject;
        String bodyTitle;

        switch (purpose) {
            case REGISTER -> {
                subject = "[Daily Quest Helper] 회원가입 이메일 인증코드";
                bodyTitle = "회원가입 이메일 인증코드";
            }
            case FIND_USERNAME -> {
                subject = "[Daily Quest Helper] 아이디 찾기 인증코드";
                bodyTitle = "아이디 찾기 인증코드";
            }
            case RESET_PASSWORD -> {
                subject = "[Daily Quest Helper] 비밀번호 재설정 인증코드";
                bodyTitle = "비밀번호 재설정 인증코드";
            }
            default -> {
                subject = "[Daily Quest Helper] 이메일 인증코드";
                bodyTitle = "이메일 인증코드";
            }
        }

        String content = buildVerificationBody(bodyTitle, code);

        try {
            log.info("Gmail API 인증 메일 발송 시도: to={}, purpose={}", toEmail, purpose);

            MimeMessage mimeMessage = createMimeMessage(toEmail, subject, content);
            Message gmailMessage = createGmailMessage(mimeMessage);

            gmail.users()
                    .messages()
                    .send("me", gmailMessage)
                    .execute();

            log.info("Gmail API 인증 메일 발송 성공: to={}", toEmail);
        } catch (Exception e) {
            log.error("Gmail API 인증 메일 발송 실패: to={}, purpose={}, error={}",
                    toEmail, purpose, e.getMessage(), e);
            throw new IllegalStateException("인증 이메일 발송에 실패했습니다.", e);
        }
    }

    private MimeMessage createMimeMessage(String toEmail, String subject, String content) throws Exception {
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name()));
        mimeMessage.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toEmail));
        mimeMessage.setSubject(subject, StandardCharsets.UTF_8.name());
        mimeMessage.setText(content, StandardCharsets.UTF_8.name());

        return mimeMessage;
    }

    private Message createGmailMessage(MimeMessage mimeMessage) throws MessagingException, java.io.IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);

        String encodedEmail = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(buffer.toByteArray());

        Message message = new Message();
        message.setRaw(encodedEmail);

        return message;
    }

    private String buildVerificationBody(String bodyTitle, String code) {
        return bodyTitle + "\n\n" +
                "인증코드: " + code + "\n" +
                "유효시간: 5분\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해주세요.";
    }
}