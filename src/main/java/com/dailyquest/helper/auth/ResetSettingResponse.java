package com.dailyquest.helper.dto;

public class ResetSettingResponse {

    private String dailyResetTime;
    private String weeklyResetTime;

    public ResetSettingResponse() {
    }

    public ResetSettingResponse(String dailyResetTime, String weeklyResetTime) {
        this.dailyResetTime = dailyResetTime;
        this.weeklyResetTime = weeklyResetTime;
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