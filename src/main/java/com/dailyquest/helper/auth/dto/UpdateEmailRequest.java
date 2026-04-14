    package com.dailyquest.helper.auth.dto;

public class UpdateEmailRequest {

    private String newEmail;
    private String password;

    public String getNewEmail() {
        return newEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}