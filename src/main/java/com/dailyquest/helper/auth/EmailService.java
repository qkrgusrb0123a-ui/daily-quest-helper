package com.dailyquest.helper.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api.key}") String resendApiKey,
            @Value("${resend.from.email}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "DailyQuestHelper/1.0")
                .build();
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

        String textBody =
                bodyTitle + "\n\n" +
                "인증코드: " + code + "\n" +
                "유효시간: 5분\n\n" +
                "본인이 요청하지 않았다면 이 메일을 무시해주세요.";

        Map<String, Object> requestBody = Map.of(
                "from", fromEmail,
                "to", List.of(email),
                "subject", title,
                "text", textBody
        );

        try {
            log.info("인증 메일 발송 시도(Resend): to={}, purpose={}", email, purpose);

            Map<?, ?> response = restClient.post()
                    .uri("/emails")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            Object emailId = (response != null) ? response.get("id") : null;

            log.info("인증 메일 발송 성공(Resend): to={}, emailId={}", email, emailId);

        } catch (RestClientResponseException e) {
            log.error(
                    "인증 메일 발송 실패(Resend): to={}, purpose={}, status={}, body={}",
                    email,
                    purpose,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            throw new RuntimeException("인증 메일 발송 실패: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("인증 메일 발송 실패(Resend): to={}, purpose={}, error={}",
                    email, purpose, e.getMessage(), e);
            throw new RuntimeException("인증 메일 발송에 실패했습니다.", e);
        }
    }
}