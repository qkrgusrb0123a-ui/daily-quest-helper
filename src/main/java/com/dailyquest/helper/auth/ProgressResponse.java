package com.dailyquest.helper.dto.auth;

public class ProgressResponse {
    private double dailyProgress;
    private double weeklyProgress;

    public ProgressResponse(double dailyProgress, double weeklyProgress) {
        this.dailyProgress = dailyProgress;
        this.weeklyProgress = weeklyProgress;
    }

    public double getDailyProgress() {
        return dailyProgress;
    }

    public double getWeeklyProgress() {
        return weeklyProgress;
    }
}