package com.dailyquest.helper.repository;

import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.entity.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByGame(Game game);
    List<Quest> findByType(QuestType type);
    List<Quest> findByGameAndType(Game game, QuestType type);
}