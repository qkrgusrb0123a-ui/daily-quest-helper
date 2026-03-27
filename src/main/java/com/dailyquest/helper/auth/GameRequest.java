package com.dailyquest.helper.dto.auth;

public class GameRequest {

    private String name;
    private String dailyResetTime;
    private String weeklyResetTime;

    public GameRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDailyResetTime() {
        return dailyResetTime;
    }

    public String getWeeklyResetTime() {
        return weeklyResetTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDailyResetTime(String dailyResetTime) {
        this.dailyResetTime = dailyResetTime;
    }

    public void setWeeklyResetTime(String weeklyResetTime) {
        this.weeklyResetTime = weeklyResetTime;
    }
}