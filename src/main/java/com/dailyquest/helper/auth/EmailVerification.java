package com.dailyquest.helper.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationPurpose purpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected EmailVerification() {
    }

    public EmailVerification(String email, String code, VerificationPurpose purpose, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matchesCode(String inputCode) {
        return code != null && code.equals(inputCode);
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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPurpose(VerificationPurpose purpose) {
        this.purpose = purpose;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}