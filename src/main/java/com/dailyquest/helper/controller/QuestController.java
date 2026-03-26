package com.dailyquest.helper.controller;

import com.dailyquest.helper.dto.ProgressResponse;
import com.dailyquest.helper.dto.QuestRequest;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.service.QuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@CrossOrigin(origins = "*")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/game/{gameId}")
    public List<Quest> getQuestsByGame(@PathVariable Long gameId) {
        return questService.getQuestsByGame(gameId);
    }

    @PostMapping
    public Quest addQuest(@RequestBody QuestRequest request) {
        return questService.addQuest(request.getGameId(), request.getContent(), request.getType());
    }

    @PutMapping("/{id}/toggle")
    public Quest toggleQuest(@PathVariable Long id) {
        return questService.toggleQuest(id);
    }

    @DeleteMapping("/{id}")
    public void deleteQuest(@PathVariable Long id) {
        questService.deleteQuest(id);
    }

    @GetMapping("/progress")
    public ProgressResponse getProgress() {
        return questService.calculateProgress();
    }
}