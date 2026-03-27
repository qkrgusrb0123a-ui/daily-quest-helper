package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.GameRequest;
import com.dailyquest.helper.auth.GameResetTimeRequest;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.service.QuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class GameController {

    private final QuestService questService;

    public GameController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping
    public List<Game> getAllGames() {
        return questService.getAllGames();
    }

    @PostMapping
    public Game addGame(@RequestBody GameRequest request) {
        return questService.addGame(
                request.getName(),
                request.getDailyResetTime(),
                request.getWeeklyResetTime()
        );
    }

    @PutMapping("/{gameId}/reset-times")
    public Game updateResetTimes(@PathVariable Long gameId, @RequestBody GameResetTimeRequest request) {
        return questService.updateGameResetTimes(
                gameId,
                request.getDailyResetTime(),
                request.getWeeklyResetTime()
        );
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        questService.deleteGame(id);
    }
}