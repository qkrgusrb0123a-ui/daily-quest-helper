package com.dailyquest.helper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String dailyResetTime = "00:00";
    private String weeklyResetTime = "00:00";

    private String lastDailyResetDate = "";
    private String lastWeeklyResetDate = "";

    @JsonIgnore
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quest> quests = new ArrayList<>();

    public Game() {
    }

    public Game(String name) {
        this.name = name;
        this.dailyResetTime = "00:00";
        this.weeklyResetTime = "00:00";
        this.lastDailyResetDate = "";
        this.lastWeeklyResetDate = "";
    }

    public Long getId() {
        return id;
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

    public String getLastDailyResetDate() {
        return lastDailyResetDate;
    }

    public String getLastWeeklyResetDate() {
        return lastWeeklyResetDate;
    }

    public List<Quest> getQuests() {
        return quests;
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

    public void setLastDailyResetDate(String lastDailyResetDate) {
        this.lastDailyResetDate = lastDailyResetDate;
    }

    public void setLastWeeklyResetDate(String lastWeeklyResetDate) {
        this.lastWeeklyResetDate = lastWeeklyResetDate;
    }
}