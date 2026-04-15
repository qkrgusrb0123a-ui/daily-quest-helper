package com.dailyquest.helper.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            VerificationPurpose purpose
    );

    @Transactional
    void deleteByEmailAndPurpose(
            String email,
            VerificationPurpose purpose
    );
}