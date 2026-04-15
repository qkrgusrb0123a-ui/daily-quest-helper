package com.dailyquest.helper.auth;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        validateEmailConfig(toEmail);

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
            log.info("Gmail API 인증 메일 발송 시도: from={}, to={}, purpose={}", senderEmail, toEmail, purpose);

            MimeMessage mimeMessage = createMimeMessage(toEmail, subject, content);
            Message gmailMessage = createGmailMessage(mimeMessage);

            gmail.users()
                    .messages()
                    .send("me", gmailMessage)
                    .execute();

            log.info("Gmail API 인증 메일 발송 성공: to={}", toEmail);
        } catch (Exception e) {
            log.error("Gmail API 인증 메일 발송 실패: from={}, to={}, purpose={}, errorType={}, error={}",
                    senderEmail, toEmail, purpose, e.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("인증 이메일 발송에 실패했습니다. Gmail API 설정값과 발신자 이메일을 확인해주세요.", e);
        }
    }

    private void validateEmailConfig(String toEmail) {
        if (isBlank(senderEmail)) {
            throw new IllegalStateException("gmail.sender.email 값이 비어 있습니다.");
        }
        if (isBlank(toEmail)) {
            throw new IllegalArgumentException("수신 이메일이 비어 있습니다.");
        }

        try {
            InternetAddress fromAddress = new InternetAddress(senderEmail, true);
            fromAddress.validate();

            InternetAddress toAddress = new InternetAddress(toEmail, true);
            toAddress.validate();
        } catch (AddressException e) {
            throw new IllegalStateException("이메일 주소 형식이 올바르지 않습니다. 발신자 또는 수신자 이메일을 확인해주세요.", e);
        }
    }

    private MimeMessage createMimeMessage(String toEmail, String subject, String content) throws Exception {
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name()));
        mimeMessage.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toEmail, true));
        mimeMessage.setSubject(subject, StandardCharsets.UTF_8.name());
        mimeMessage.setText(content, StandardCharsets.UTF_8.name());
        mimeMessage.saveChanges();

        return mimeMessage;
    }

    private Message createGmailMessage(MimeMessage mimeMessage) throws MessagingException, IOException {
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}