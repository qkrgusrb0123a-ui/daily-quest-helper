package com.dailyquest.helper.auth;

import com.dailyquest.helper.auth.dto.LoginRequest;
import com.dailyquest.helper.auth.dto.RegisterRequest;
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
@Transactional
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

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        createAndSendVerificationCode(normalizedEmail, VerificationPurpose.REGISTER);
    }

    public void confirmRegisterVerificationCode(String email, String verificationCode) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = verificationCode == null ? "" : verificationCode.trim();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException("이메일 인증코드를 입력해주세요.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        EmailVerification verification = getValidVerificationOrThrow(
                normalizedEmail,
                normalizedCode,
                VerificationPurpose.REGISTER
        );

        verification.setUsed(true);
        emailVerificationRepository.save(verification);
    }

    public void sendFindUsernameVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("사용자 계정이 없습니다.");
        }

        createAndSendVerificationCode(normalizedEmail, VerificationPurpose.FIND_USERNAME);
    }

    public void sendResetPasswordVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("사용자 계정이 없습니다.");
        }

        createAndSendVerificationCode(normalizedEmail, VerificationPurpose.RESET_PASSWORD);
    }

    public void register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = normalizeEmail(request.getEmail());
        String verificationCode = request.getVerificationCode() == null ? "" : request.getVerificationCode().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (username.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (verificationCode.isBlank()) {
            throw new IllegalArgumentException("이메일 인증코드를 입력해주세요.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        EmailVerification verification = getValidVerificationOrThrow(
                email,
                verificationCode,
                VerificationPurpose.REGISTER
        );

        if (!Boolean.TRUE.equals(verification.getUsed())) {
            throw new IllegalArgumentException("인증확인 버튼을 눌러 이메일 인증을 완료해주세요.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePassword(password);

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(username, email, encodedPassword, true);
        userRepository.save(user);

        emailVerificationRepository.deleteByEmailAndPurpose(email, VerificationPurpose.REGISTER);
    }

    public String findUsernameByEmail(String email, String verificationCode) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = verificationCode == null ? "" : verificationCode.trim();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException("이메일 인증코드를 입력해주세요.");
        }

        verifyCodeOrThrow(normalizedEmail, normalizedCode, VerificationPurpose.FIND_USERNAME);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 계정이 없습니다."));

        emailVerificationRepository.deleteByEmailAndPurpose(normalizedEmail, VerificationPurpose.FIND_USERNAME);

        return user.getUsername();
    }

    public void resetPasswordByEmail(String email, String verificationCode, String newPassword, String confirmPassword) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = verificationCode == null ? "" : verificationCode.trim();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException("이메일 인증코드를 입력해주세요.");
        }

        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인을 입력해주세요.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        verifyCodeOrThrow(normalizedEmail, normalizedCode, VerificationPurpose.RESET_PASSWORD);

        validatePassword(newPassword);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 계정이 없습니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailVerificationRepository.deleteByEmailAndPurpose(normalizedEmail, VerificationPurpose.RESET_PASSWORD);
    }

    public void login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
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

    private void createAndSendVerificationCode(String email, VerificationPurpose purpose) {
        emailVerificationRepository.deleteByEmailAndPurpose(email, purpose);

        String code = generateVerificationCode();

        EmailVerification verification = new EmailVerification(
                email,
                code,
                purpose,
                LocalDateTime.now().plusMinutes(5)
        );

        verification.setUsed(false);
        emailVerificationRepository.save(verification);

        try {
            emailService.sendVerificationCode(email, code, purpose);
        } catch (RuntimeException e) {
            emailVerificationRepository.deleteByEmailAndPurpose(email, purpose);
            throw e;
        }
    }

    private void verifyCodeOrThrow(String email, String inputCode, VerificationPurpose purpose) {
        getValidVerificationOrThrow(email, inputCode, purpose);
    }

    private EmailVerification getValidVerificationOrThrow(String email, String inputCode, VerificationPurpose purpose) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new IllegalArgumentException("인증코드를 먼저 요청해주세요."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 다시 요청해주세요.");
        }

        if (!verification.matchesCode(inputCode)) {
            throw new IllegalArgumentException("인증코드가 올바르지 않습니다.");
        }

        return verification;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
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