package com.dailyquest.helper.service;

import com.dailyquest.helper.dto.ProgressResponse;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.entity.QuestType;
import com.dailyquest.helper.repository.GameRepository;
import com.dailyquest.helper.repository.QuestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class QuestService {

    private final GameRepository gameRepository;
    private final QuestRepository questRepository;

    public QuestService(GameRepository gameRepository, QuestRepository questRepository) {
        this.gameRepository = gameRepository;
        this.questRepository = questRepository;
    }

    public Game addGame(String name, String dailyResetTime, String weeklyResetTime) {
        Game game = new Game(name);
        game.setDailyResetTime(isBlank(dailyResetTime) ? "00:00" : validateTime(dailyResetTime));
        game.setWeeklyResetTime(isBlank(weeklyResetTime) ? "00:00" : validateTime(weeklyResetTime));
        game.setLastDailyResetDate("");
        game.setLastWeeklyResetDate("");
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public void deleteGame(Long gameId) {
        gameRepository.deleteById(gameId);
    }

    public Game updateGameResetTimes(Long gameId, String dailyResetTime, String weeklyResetTime) {
        Game game = gameRepository.findById(gameId).orElseThrow();

        String validatedDaily = validateTime(dailyResetTime);
        String validatedWeekly = validateTime(weeklyResetTime);

        boolean dailyChanged = !validatedDaily.equals(game.getDailyResetTime());
        boolean weeklyChanged = !validatedWeekly.equals(game.getWeeklyResetTime());

        game.setDailyResetTime(validatedDaily);
        game.setWeeklyResetTime(validatedWeekly);

        // 시간이 변경되면 마지막 초기화 날짜를 비워서 새 설정 기준으로 다시 체크되게 함
        if (dailyChanged) {
            game.setLastDailyResetDate("");
        }

        if (weeklyChanged) {
            game.setLastWeeklyResetDate("");
        }

        return gameRepository.save(game);
    }

    public Quest addQuest(Long gameId, String content, String type) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Quest quest = new Quest(content, false, QuestType.valueOf(type), game);
        return questRepository.save(quest);
    }

    public List<Quest> getQuestsByGame(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        return questRepository.findByGame(game);
    }

    public Quest toggleQuest(Long questId) {
        Quest quest = questRepository.findById(questId).orElseThrow();
        quest.setCompleted(!quest.isCompleted());
        return questRepository.save(quest);
    }

    public void deleteQuest(Long questId) {
        questRepository.deleteById(questId);
    }

    public ProgressResponse calculateProgress() {
        List<Quest> allQuests = questRepository.findAll();

        long dailyTotal = allQuests.stream()
                .filter(q -> q.getType() == QuestType.DAILY)
                .count();

        long dailyCompleted = allQuests.stream()
                .filter(q -> q.getType() == QuestType.DAILY && q.isCompleted())
                .count();

        long weeklyTotal = allQuests.stream()
                .filter(q -> q.getType() == QuestType.WEEKLY)
                .count();

        long weeklyCompleted = allQuests.stream()
                .filter(q -> q.getType() == QuestType.WEEKLY && q.isCompleted())
                .count();

        double dailyProgress = dailyTotal == 0 ? 0 : (dailyCompleted * 100.0 / dailyTotal);
        double weeklyProgress = weeklyTotal == 0 ? 0 : (weeklyCompleted * 100.0 / weeklyTotal);

        return new ProgressResponse(dailyProgress, weeklyProgress);
    }

    public void resetDailyQuestsByGame(Game game) {
        List<Quest> quests = questRepository.findByGameAndType(game, QuestType.DAILY);

        for (Quest quest : quests) {
            quest.setCompleted(false);
        }

        questRepository.saveAll(quests);
    }

    public void resetWeeklyQuestsByGame(Game game) {
        List<Quest> quests = questRepository.findByGameAndType(game, QuestType.WEEKLY);

        for (Quest quest : quests) {
            quest.setCompleted(false);
        }

        questRepository.saveAll(quests);
    }

    private String validateTime(String timeText) {
        if (isBlank(timeText)) {
            throw new IllegalArgumentException("초기화 시간을 입력해야 합니다.");
        }

        LocalTime parsed = LocalTime.parse(timeText);
        return String.format("%02d:%02d", parsed.getHour(), parsed.getMinute());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}