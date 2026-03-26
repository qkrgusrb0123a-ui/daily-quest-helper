package com.dailyquest.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DailyQuestHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyQuestHelperApplication.class, args);
    }
}