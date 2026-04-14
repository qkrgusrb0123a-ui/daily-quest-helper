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

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GameRepository gameRepository;
    private final QuestRepository questRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       GameRepository gameRepository,
                       QuestRepository questRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.gameRepository = gameRepository;
        this.questRepository = questRepository;
    }

    public void register(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (username.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePassword(password);

        String encodedPassword = passwordEncoder.encode(password);

        /*
         이메일 기능 임시 비활성화
         users.email 컬럼이 NOT NULL + UNIQUE 이므로
         내부 저장용 임시 이메일 값을 자동 생성
        */
        String placeholderEmail = "disabled_" + UUID.randomUUID() + "@disabled.local";

        User user = new User(username, placeholderEmail, encodedPassword, true);
        userRepository.save(user);
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