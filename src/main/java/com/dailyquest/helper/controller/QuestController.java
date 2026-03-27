package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.ProgressResponse;
import com.dailyquest.helper.auth.QuestRequest;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.service.QuestService;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestService questService;
    private final UserService userService;

    public QuestController(QuestService questService, UserService userService) {
        this.questService = questService;
        this.userService = userService;
    }

    @GetMapping("/game/{gameId}")
    public List<Quest> getQuestsByGame(@PathVariable Long gameId, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.getQuestsByGame(gameId, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping
    public Quest addQuest(@RequestBody QuestRequest request, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.addQuest(
                    request.getGameId(),
                    request.getContent(),
                    request.getType(),
                    user
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public Quest toggleQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.toggleQuest(id, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            questService.deleteQuest(id, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/progress")
    public ProgressResponse getProgress(HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return questService.calculateProgress(user);
    }
}