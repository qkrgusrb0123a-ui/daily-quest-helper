package com.dailyquest.helper.dto;

public class GameResetTimeRequest {

    private String dailyResetTime;
    private String weeklyResetTime;

    public GameResetTimeRequest() {
    }

    public String getDailyResetTime() {
        return dailyResetTime;
    }

    public String getWeeklyResetTime() {
        return weeklyResetTime;
    }

    public void setDailyResetTime(String dailyResetTime) {
        this.dailyResetTime = dailyResetTime;
    }

    public void setWeeklyResetTime(String weeklyResetTime) {
        this.weeklyResetTime = weeklyResetTime;
    }
}