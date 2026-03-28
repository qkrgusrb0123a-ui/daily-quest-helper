package com.dailyquest.helper.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    List<EmailVerification> findByEmailAndPurposeAndUsedFalse(String email, VerificationPurpose purpose);

    Optional<EmailVerification> findTopByEmailAndPurposeAndCodeAndUsedFalseOrderByIdDesc(
            String email,
            VerificationPurpose purpose,
            String code
    );
}