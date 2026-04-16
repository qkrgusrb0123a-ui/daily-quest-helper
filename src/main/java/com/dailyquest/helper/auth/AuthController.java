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

    @PostMapping("/send-register-code")
    public ResponseEntity<?> sendRegisterCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.sendRegisterVerificationCode(email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입 인증코드를 전송했습니다."
        ));
    }

    @PostMapping("/confirm-register-code")
    public ResponseEntity<?> confirmRegisterCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("verificationCode");

        authService.confirmRegisterVerificationCode(email, verificationCode);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이메일 인증이 완료되었습니다."
        ));
    }

    @PostMapping("/send-find-username-code")
    public ResponseEntity<?> sendFindUsernameCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.sendFindUsernameVerificationCode(email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "아이디 찾기 인증코드를 전송했습니다."
        ));
    }

    @PostMapping("/send-reset-password-code")
    public ResponseEntity<?> sendResetPasswordCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.sendResetPasswordVerificationCode(email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호 재설정 인증코드를 전송했습니다."
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다."
        ));
    }

    @PostMapping("/find-username")
    public ResponseEntity<?> findUsername(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("verificationCode");

        String username = authService.findUsernameByEmail(email, verificationCode);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "아이디 찾기에 성공했습니다.",
                "username", username
        ));
    }

    @PostMapping("/reset-password-by-email")
    public ResponseEntity<?> resetPasswordByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("verificationCode");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        authService.resetPasswordByEmail(email, verificationCode, newPassword, confirmPassword);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 재설정되었습니다."
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

    @GetMapping("/email")
    public ResponseEntity<?> getMyEmail(HttpSession session) {
        Long userId = getLoginUserId(session);
        User user = authService.getUserById(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "email", user.getEmail()
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