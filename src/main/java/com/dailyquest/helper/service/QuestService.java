package com.dailyquest.helper.service;

import com.dailyquest.helper.auth.ProgressResponse;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.entity.QuestType;
import com.dailyquest.helper.repository.GameRepository;
import com.dailyquest.helper.repository.QuestRepository;
import com.dailyquest.helper.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (isBlank(name)) {
            throw new IllegalArgumentException("게임 이름을 입력해주세요.");
        }

        Game game = new Game(name.trim());
        game.setUser(user);
        game.setDailyResetTime(isBlank(dailyResetTime) ? "00:00" : validateTime(dailyResetTime));
        game.setWeeklyResetTime(isBlank(weeklyResetTime) ? "00:00" : validateTime(weeklyResetTime));
        game.setLastDailyResetDate("");
        game.setLastWeeklyResetDate("");

        return gameRepository.save(game);
    }

    public List<Game> getAllGames(User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return gameRepository.findByUser(user);
    }

    public Game updateGameName(Long gameId, String name, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("게임 이름을 입력해주세요.");
        }

        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        game.setName(name.trim());
        return gameRepository.save(game);
    }

    public void deleteGame(Long gameId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));
        gameRepository.delete(game);
    }

    public Game updateGameResetTimes(Long gameId, String dailyResetTime, String weeklyResetTime, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

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
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (gameId == null) {
            throw new IllegalArgumentException("게임 정보를 찾을 수 없습니다.");
        }

        if (isBlank(content)) {
            throw new IllegalArgumentException("퀘스트 내용을 입력해주세요.");
        }

        if (isBlank(type)) {
            throw new IllegalArgumentException("퀘스트 타입이 필요합니다.");
        }

        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        QuestType questType;
        try {
            questType = QuestType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("퀘스트 타입이 올바르지 않습니다.");
        }

        int nextOrder = questRepository.findByUserAndGameAndTypeOrderBySortOrderAsc(user, game, questType).size();

        Quest quest = new Quest(content.trim(), false, questType, game);
        quest.setUser(user);
        quest.setSortOrder(nextOrder);

        return questRepository.save(quest);
    }

    public List<Quest> getQuestsByGame(Long gameId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Game game = gameRepository.findByIdAndUser(gameId, user)
                .orElseThrow(() -> new IllegalArgumentException("게임 정보를 찾을 수 없습니다."));

        return questRepository.findByUserAndGameOrderBySortOrderAsc(user, game);
    }

    public Quest toggleQuest(Long questId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Quest quest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        quest.setCompleted(!quest.isCompleted());
        return questRepository.save(quest);
    }

    public Quest updateQuestContent(Long questId, String content, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("퀘스트 내용을 입력해주세요.");
        }

        Quest quest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        quest.setContent(content.trim());
        return questRepository.save(quest);
    }

    @Transactional
    public void updateQuestOrder(Long questId, int newIndex, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Quest targetQuest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        List<Quest> sameTypeQuests = questRepository.findByUserAndGameAndTypeOrderBySortOrderAsc(
                user,
                targetQuest.getGame(),
                targetQuest.getType()
        );

        int currentIndex = -1;
        for (int i = 0; i < sameTypeQuests.size(); i++) {
            if (sameTypeQuests.get(i).getId().equals(targetQuest.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new IllegalArgumentException("퀘스트 순서를 변경할 수 없습니다.");
        }

        int boundedIndex = Math.max(0, Math.min(newIndex, sameTypeQuests.size() - 1));
        if (currentIndex == boundedIndex) {
            return;
        }

        Quest moved = sameTypeQuests.remove(currentIndex);
        sameTypeQuests.add(boundedIndex, moved);

        for (int i = 0; i < sameTypeQuests.size(); i++) {
            sameTypeQuests.get(i).setSortOrder(i);
        }

        questRepository.saveAll(sameTypeQuests);
    }

    public void deleteQuest(Long questId, User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Quest quest = questRepository.findByIdAndUser(questId, user)
                .orElseThrow(() -> new IllegalArgumentException("퀘스트 정보를 찾을 수 없습니다."));

        Game game = quest.getGame();
        QuestType type = quest.getType();
        questRepository.delete(quest);
        normalizeQuestOrders(user, game, type);
    }

    public ProgressResponse calculateProgress(User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

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
        List<Quest> quests = questRepository.findByUserAndGameAndTypeOrderBySortOrderAsc(user, game, QuestType.DAILY);

        for (Quest quest : quests) {
            quest.setCompleted(false);
        }

        questRepository.saveAll(quests);
    }

    public void resetWeeklyQuestsByGame(Game game) {
        User user = game.getUser();
        List<Quest> quests = questRepository.findByUserAndGameAndTypeOrderBySortOrderAsc(user, game, QuestType.WEEKLY);

        for (Quest quest : quests) {
            quest.setCompleted(false);
        }

        questRepository.saveAll(quests);
    }

    private void normalizeQuestOrders(User user, Game game, QuestType type) {
        List<Quest> quests = questRepository.findByUserAndGameAndTypeOrderBySortOrderAsc(user, game, type);
        for (int i = 0; i < quests.size(); i++) {
            quests.get(i).setSortOrder(i);
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
