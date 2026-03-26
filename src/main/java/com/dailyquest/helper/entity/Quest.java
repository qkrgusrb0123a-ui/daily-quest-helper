package com.dailyquest.helper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private boolean completed;

    @Enumerated(EnumType.STRING)
    private QuestType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @JsonIgnore
    private Game game;

    public Quest() {
    }

    public Quest(String content, boolean completed, QuestType type, Game game) {
        this.content = content;
        this.completed = completed;
        this.type = type;
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public QuestType getType() {
        return type;
    }

    public Game getGame() {
        return game;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}