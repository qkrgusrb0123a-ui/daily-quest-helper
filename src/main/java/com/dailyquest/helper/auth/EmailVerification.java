package com.dailyquest.helper.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationPurpose purpose;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String code, VerificationPurpose purpose, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean matchesCode(String inputCode) {
        return code != null && code.equals(inputCode);
    }
}