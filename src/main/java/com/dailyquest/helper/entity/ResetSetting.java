package com.dailyquest.helper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ResetSetting {

    @Id
    private Long id;

    private String dailyResetTime;   // HH:mm
    private String weeklyResetTime;  // HH:mm

    private String lastDailyResetDate;   // yyyy-MM-dd
    private String lastWeeklyResetDate;  // yyyy-MM-dd

    public ResetSetting() {
    }

    public ResetSetting(Long id, String dailyResetTime, String weeklyResetTime,
                        String lastDailyResetDate, String lastWeeklyResetDate) {
        this.id = id;
        this.dailyResetTime = dailyResetTime;
        this.weeklyResetTime = weeklyResetTime;
        this.lastDailyResetDate = lastDailyResetDate;
        this.lastWeeklyResetDate = lastWeeklyResetDate;
    }

    public Long getId() {
        return id;
    }

    public String getDailyResetTime() {
        return dailyResetTime;
    }

    public String getWeeklyResetTime() {
        return weeklyResetTime;
    }

    public String getLastDailyResetDate() {
        return lastDailyResetDate;
    }

    public String getLastWeeklyResetDate() {
        return lastWeeklyResetDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDailyResetTime(String dailyResetTime) {
        this.dailyResetTime = dailyResetTime;
    }

    public void setWeeklyResetTime(String weeklyResetTime) {
        this.weeklyResetTime = weeklyResetTime;
    }

    public void setLastDailyResetDate(String lastDailyResetDate) {
        this.lastDailyResetDate = lastDailyResetDate;
    }

    public void setLastWeeklyResetDate(String lastWeeklyResetDate) {
        this.lastWeeklyResetDate = lastWeeklyResetDate;
    }
}