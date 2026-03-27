package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.GameRequest;
import com.dailyquest.helper.auth.GameResetTimeRequest;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.service.QuestService;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class GameController {

    private final QuestService questService;
    private final UserService userService;

    public GameController(QuestService questService, UserService userService) {
        this.questService = questService;
        this.userService = userService;
    }

    @GetMapping
    public List<Game> getAllGames(HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.getAllGames(user);
    }

    @PostMapping
    public Game addGame(@RequestBody GameRequest request, HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.addGame(
                request.getName(),
                request.getDailyResetTime(),
                request.getWeeklyResetTime(),
                user
        );
    }

    @PutMapping("/{gameId}/reset-times")
    public Game updateResetTimes(@PathVariable Long gameId,
                                 @RequestBody GameResetTimeRequest request,
                                 HttpSession session) {
        User user = userService.getLoginUser(session);
        return questService.updateGameResetTimes(
                gameId,
                request.getDailyResetTime(),
                request.getWeeklyResetTime(),
                user
        );
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);
        questService.deleteGame(id, user);
    }
}