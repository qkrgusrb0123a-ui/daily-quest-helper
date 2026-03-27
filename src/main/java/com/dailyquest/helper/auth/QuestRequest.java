package com.dailyquest.helper.auth;

public class QuestRequest {
    private String content;
    private String type;   // DAILY or WEEKLY
    private Long gameId;

    public QuestRequest() {
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}