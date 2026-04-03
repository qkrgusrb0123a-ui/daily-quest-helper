package com.dailyquest.helper.service;

import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.repository.GameRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class GameResetScheduler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final GameRepository gameRepository;
    private final QuestService questService;

    public GameResetScheduler(GameRepository gameRepository, QuestService questService) {
        this.gameRepository = gameRepository;
        this.questService = questService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndResetQuests() {
        List<Game> games = gameRepository.findAll();

        LocalDate today = LocalDate.now(KOREA_ZONE);
        LocalTime now = LocalTime.now(KOREA_ZONE).withSecond(0).withNano(0);
        String todayText = today.toString();

        System.out.println("[Scheduler] now = " + now + ", games = " + games.size());

        for (Game game : games) {
            try {
                if (game.getUser() == null) {
                    System.out.println("[Scheduler] skip game because user is null: " + game.getName());
                    continue;
                }

                boolean changed = false;

                String dailyText = game.getDailyResetTime();
                String weeklyText = game.getWeeklyResetTime();

                if (dailyText == null || dailyText.isBlank()) {
                    dailyText = "00:00";
                    game.setDailyResetTime(dailyText);
                    changed = true;
                }

                if (weeklyText == null || weeklyText.isBlank()) {
                    weeklyText = "00:00";
                    game.setWeeklyResetTime(weeklyText);
                    changed = true;
                }

                if (game.getLastDailyResetDate() == null) {
                    game.setLastDailyResetDate("");
                    changed = true;
                }

                if (game.getLastWeeklyResetDate() == null) {
                    game.setLastWeeklyResetDate("");
                    changed = true;
                }

                LocalTime dailyResetTime;
                LocalTime weeklyResetTime;

                try {
                    dailyResetTime = LocalTime.parse(dailyText);
                    weeklyResetTime = LocalTime.parse(weeklyText);
                } catch (Exception e) {
                    System.out.println("[Scheduler] invalid reset time for game: " + game.getName()
                            + ", dailyResetTime=" + dailyText
                            + ", weeklyResetTime=" + weeklyText);
                    continue;
                }

                if (!todayText.equals(game.getLastDailyResetDate()) && !now.isBefore(dailyResetTime)) {
                    System.out.println("[Scheduler] Daily reset: " + game.getName());
                    questService.resetDailyQuestsByGame(game);
                    game.setLastDailyResetDate(todayText);
                    changed = true;
                }

                boolean isWeeklyResetDay = today.getDayOfWeek() == DayOfWeek.MONDAY;

                if (isWeeklyResetDay
                        && !todayText.equals(game.getLastWeeklyResetDate())
                        && !now.isBefore(weeklyResetTime)) {
                    System.out.println("[Scheduler] Weekly reset: " + game.getName());
                    questService.resetWeeklyQuestsByGame(game);
                    game.setLastWeeklyResetDate(todayText);
                    changed = true;
                }

                if (changed) {
                    gameRepository.save(game);
                }

            } catch (Exception e) {
                System.out.println("[Scheduler] reset error for game: " + game.getName()
                        + ", dailyResetTime=" + game.getDailyResetTime()
                        + ", weeklyResetTime=" + game.getWeeklyResetTime()
                        + ", lastDailyResetDate=" + game.getLastDailyResetDate()
                        + ", lastWeeklyResetDate=" + game.getLastWeeklyResetDate());
                e.printStackTrace();
            }
        }
    }
}