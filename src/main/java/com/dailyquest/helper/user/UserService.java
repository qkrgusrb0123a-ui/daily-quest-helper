package com.dailyquest.helper.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getLoginUser(HttpSession session) {
        Object userIdObj = session.getAttribute("LOGIN_USER_ID");

        if (userIdObj == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long userId = Long.valueOf(userIdObj.toString());
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }
}