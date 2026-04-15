package com.dailyquest.helper.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GmailApiConfig {

    @Value("${gmail.client.id}")
    private String clientId;

    @Value("${gmail.client.secret}")
    private String clientSecret;

    @Value("${gmail.refresh.token}")
    private String refreshToken;

    @Value("${gmail.application.name:DailyQuestHelper}")
    private String applicationName;

    @Bean
    public Gmail gmail() throws Exception {
        validateRequiredValues();

        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(transport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(clientId, clientSecret)
                .build();

        credential.setRefreshToken(refreshToken);

        boolean refreshed = credential.refreshToken();
        if (!refreshed || credential.getAccessToken() == null || credential.getAccessToken().isBlank()) {
            throw new IllegalStateException("Gmail OAuth access token 갱신에 실패했습니다. client id / secret / refresh token 또는 Gmail API 권한을 확인해주세요.");
        }

        HttpRequestInitializer requestInitializer = request -> {
            credential.initialize(request);
            request.setConnectTimeout(30_000);
            request.setReadTimeout(30_000);
        };

        return new Gmail.Builder(transport, jsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

    private void validateRequiredValues() {
        if (isBlank(clientId)) {
            throw new IllegalStateException("gmail.client.id 값이 비어 있습니다.");
        }
        if (isBlank(clientSecret)) {
            throw new IllegalStateException("gmail.client.secret 값이 비어 있습니다.");
        }
        if (isBlank(refreshToken)) {
            throw new IllegalStateException("gmail.refresh.token 값이 비어 있습니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}