package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.ProgressResponse;
import com.dailyquest.helper.auth.QuestRequest;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.service.QuestService;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@CrossOrigin(origins = "*")
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
        return questService.getQuestsByGame(gameId, user);
    }

    @PostMapping
    public Quest addQuest(@RequestBody QuestRequest request, HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.addQuest(
                request.getGameId(),
                request.getContent(),
                request.getType(),
                user
        );
    }

    @PutMapping("/{id}/toggle")
    public Quest toggleQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.toggleQuest(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);
        questService.deleteQuest(id, user);
    }

    @GetMapping("/progress")
    public ProgressResponse getProgress(HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.calculateProgress(user);
    }
}