package com.dailyquest.helper.auth;

import com.dailyquest.helper.auth.dto.FindUsernameRequest;
import com.dailyquest.helper.auth.dto.LoginRequest;
import com.dailyquest.helper.auth.dto.RegisterRequest;
import com.dailyquest.helper.auth.dto.ResetPasswordByEmailRequest;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.repository.GameRepository;
import com.dailyquest.helper.repository.QuestRepository;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GameRepository gameRepository;
    private final QuestRepository questRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       GameRepository gameRepository,
                       QuestRepository questRepository,
                       EmailVerificationRepository emailVerificationRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gameRepository = gameRepository;
        this.questRepository = questRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
    }

    public void sendRegisterVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        issueVerificationCode(normalizedEmail, VerificationPurpose.REGISTER);
    }

    public void sendFindUsernameVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("사용자 계정이 없습니다.");
        }

        issueVerificationCode(normalizedEmail, VerificationPurpose.FIND_USERNAME);
    }

    public void sendResetPasswordVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("사용자 계정이 없습니다.");
        }

        issueVerificationCode(normalizedEmail, VerificationPurpose.RESET_PASSWORD);
    }

    public void register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();
        String code = request.getEmailVerificationCode() == null ? "" : request.getEmailVerificationCode().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePassword(password);

        verifyAndUseCode(email, code, VerificationPurpose.REGISTER);

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(username, email, encodedPassword, true);
        userRepository.save(user);
    }

    public void login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않은 계정입니다.");
        }

        session.setAttribute("LOGIN_USER_ID", user.getId());
        session.setAttribute("LOGIN_USERNAME", user.getUsername());
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public String findMaskedUsername(FindUsernameRequest request) {
        String email = normalizeEmail(request.getEmail());
        String code = request.getVerificationCode() == null ? "" : request.getVerificationCode().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 계정이 없습니다."));

        verifyAndUseCode(email, code, VerificationPurpose.FIND_USERNAME);
        return maskUsername(user.getUsername());
    }

    public void resetPasswordByEmail(ResetPasswordByEmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        String code = request.getVerificationCode() == null ? "" : request.getVerificationCode().trim();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 계정이 없습니다."));

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePassword(newPassword);
        verifyAndUseCode(email, code, VerificationPurpose.RESET_PASSWORD);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUsername(Long userId, String newUsername) {
        String trimmedUsername = newUsername == null ? "" : newUsername.trim();

        if (trimmedUsername.isBlank()) {
            throw new IllegalArgumentException("새 아이디를 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.getUsername().equals(trimmedUsername) && userRepository.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        user.setUsername(trimmedUsername);
        userRepository.save(user);
    }

    public void updateEmail(Long userId, String newEmail, String password) {
        String normalizedEmail = normalizeEmail(newEmail);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!user.getEmail().equals(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        user.setEmail(normalizedEmail);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("기존 비밀번호를 입력해주세요.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인을 입력해주세요.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Game> userGames = gameRepository.findByUser(user);

        for (Game game : userGames) {
            List<Quest> userGameQuests = questRepository.findByUserAndGameOrderBySortOrderAsc(user, game);
            if (!userGameQuests.isEmpty()) {
                questRepository.deleteAll(userGameQuests);
            }
        }

        if (!userGames.isEmpty()) {
            gameRepository.deleteAll(userGames);
        }

        userRepository.delete(user);
    }

    @Transactional
    protected void issueVerificationCode(String email, VerificationPurpose purpose) {
        List<EmailVerification> oldCodes =
                emailVerificationRepository.findByEmailAndPurposeAndUsedFalse(email, purpose);

        for (EmailVerification oldCode : oldCodes) {
            oldCode.setUsed(true);
        }
        emailVerificationRepository.saveAll(oldCodes);

        String code = generateVerificationCode();
        EmailVerification verification = new EmailVerification(
                email,
                purpose,
                code,
                LocalDateTime.now().plusMinutes(5)
        );

        emailVerificationRepository.save(verification);
        emailService.sendVerificationCode(email, code, purpose);
    }

    @Transactional
    protected void verifyAndUseCode(String email, String code, VerificationPurpose purpose) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("이메일 인증코드를 입력해주세요.");
        }

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeAndCodeAndUsedFalseOrderByIdDesc(email, purpose, code.trim())
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증코드가 올바르지 않습니다."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이메일 인증코드가 만료되었습니다.");
        }

        verification.setUsed(true);
        emailVerificationRepository.save(verification);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
        return email.trim().toLowerCase();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int value = 100000 + random.nextInt(900000);
        return String.valueOf(value);
    }

    private String maskUsername(String username) {
        if (username == null || username.isBlank()) {
            return "";
        }

        if (username.length() <= 2) {
            return username.charAt(0) + "*";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(username.charAt(0));

        for (int i = 1; i < username.length() - 1; i++) {
            builder.append("*");
        }

        builder.append(username.charAt(username.length() - 1));
        return builder.toString();
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new IllegalArgumentException("비밀번호에는 특수문자가 최소 1개 포함되어야 합니다.");
        }

        if (containsSequentialNumbers(password)) {
            throw new IllegalArgumentException("비밀번호에는 연속된 숫자를 사용할 수 없습니다.");
        }
    }

    private boolean containsSequentialNumbers(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char a = password.charAt(i);
            char b = password.charAt(i + 1);
            char c = password.charAt(i + 2);

            if (Character.isDigit(a) && Character.isDigit(b) && Character.isDigit(c)) {
                int n1 = a - '0';
                int n2 = b - '0';
                int n3 = c - '0';

                boolean ascending = (n2 == n1 + 1) && (n3 == n2 + 1);
                boolean zeroWrap = (a == '8' && b == '9' && c == '0');
                boolean sameNumber = (a == b && b == c);
                boolean descending = (n2 == n1 - 1) && (n3 == n2 - 1);

                if (ascending || zeroWrap || sameNumber || descending) {
                    return true;
                }
            }
        }
        return false;
    }
}