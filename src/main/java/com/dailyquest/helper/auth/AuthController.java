package com.dailyquest.helper.auth;

import com.dailyquest.helper.auth.dto.LoginRequest;
import com.dailyquest.helper.auth.dto.RegisterRequest;
import com.dailyquest.helper.auth.dto.UpdatePasswordRequest;
import com.dailyquest.helper.auth.dto.UpdateUsernameRequest;
import com.dailyquest.helper.user.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     이메일 기능 임시 비활성화
     아래 API들은 주석 처리 대상이었음
     - /send-register-code
     - /send-find-username-code
     - /send-reset-password-code
     - /find-username
     - /reset-password-by-email
     - /email
    */

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다."
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        authService.login(request, session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인 성공",
                "username", session.getAttribute("LOGIN_USERNAME")
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃 되었습니다."
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = getLoginUserId(session);
        User user = authService.getUserById(userId);

        return ResponseEntity.ok(Map.of(
                "loggedIn", true,
                "userId", user.getId(),
                "username", user.getUsername()
        ));
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody UpdateUsernameRequest request, HttpSession session) {
        Long userId = getLoginUserId(session);

        authService.updateUsername(userId, request.getNewUsername());

        User user = authService.getUserById(userId);
        session.setAttribute("LOGIN_USERNAME", user.getUsername());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "아이디가 변경되었습니다.",
                "username", user.getUsername()
        ));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request, HttpSession session) {
        Long userId = getLoginUserId(session);

        authService.updatePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 변경되었습니다."
        ));
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(HttpSession session) {
        Long userId = getLoginUserId(session);

        authService.deleteAccount(userId);
        session.invalidate();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원탈퇴가 완료되었습니다."
        ));
    }

    private Long getLoginUserId(HttpSession session) {
        Object userIdObj = session.getAttribute("LOGIN_USER_ID");

        if (userIdObj == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return Long.valueOf(userIdObj.toString());
    }
}