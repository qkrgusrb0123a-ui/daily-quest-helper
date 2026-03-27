package com.dailyquest.helper.service;

import com.dailyquest.helper.auth.ProgressResponse;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.entity.QuestType;
import com.dailyquest.helper.repository.GameRepository;
import com.dailyquest.helper.repository.QuestRepository;
import com.dailyquest.helper.user.User;
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

    public Game addGame(String name, String dailyResetTime, String weeklyResetTime, User user) {
        Game game = new Game(name);
        game.setUser(user);
        game.setDailyResetTime(isBlank(dailyResetTime) ? "00:00" : validateTime(dailyResetTime));
        game.setWeeklyResetTime(isBlank(weeklyResetTime) ? "00:00" : validateTime(weeklyResetTime));
        game.setLastDailyResetDate("");
        game.setLastWeeklyResetDate("");
        return gameRepository.save(game);
    }

    public List<Game> getAllGames(User user) {
        return gameRepository.findByUser(user);
    }

    public void deleteGame(Long gameId, User user) {
        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));
        gameRepository.delete(game);
    }

    public Game updateGameResetTimes(Long gameId, String dailyResetTime, String weeklyResetTime, User user) {
        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        String validatedDaily = validateTime(dailyResetTime);
        String validatedWeekly = validateTime(weeklyResetTime);

        boolean dailyChanged = !validatedDaily.equals(game.getDailyResetTime());
        boolean weeklyChanged = !validatedWeekly.equals(game.getWeeklyResetTime());

        game.setDailyResetTime(validatedDaily);
        game.setWeeklyResetTime(validatedWeekly);

        if (dailyChanged) {
            game.setLastDailyResetDate("");
        }

        if (weeklyChanged) {
            game.setLastWeeklyResetDate("");
        }

        return gameRepository.save(game);
    }

    public Quest addQuest(Long gameId, String content, String type, User user) {
        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        Quest quest = new Quest(content, false, QuestType.valueOf(type), game);
        quest.setUser(user);

        return questRepository.save(quest);
    }

    public List<Quest> getQuestsByGame(Long gameId, User user) {
        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        return questRepository.findByUserAndGame(user, game);
    }

    public Quest toggleQuest(Long questId, User user) {
        Quest quest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        quest.setCompleted(!quest.isCompleted());
        return questRepository.save(quest);
    }

    public void deleteQuest(Long questId, User user) {
        Quest quest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        questRepository.delete(quest);
    }

    public ProgressResponse calculateProgress(User user) {
        List<Quest> allQuests = questRepository.findByUser(user);

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
        User user = game.getUser();
        List<Quest> quests = questRepository.findByUserAndGameAndType(user, game, QuestType.DAILY);

        for (Quest quest : quests) {
            quest.setCompleted(false);
        }

        questRepository.saveAll(quests);
    }

    public void resetWeeklyQuestsByGame(Game game) {
        User user = game.getUser();
        List<Quest> quests = questRepository.findByUserAndGameAndType(user, game, QuestType.WEEKLY);

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