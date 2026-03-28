package com.dailyquest.helper.repository;

import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.entity.QuestType;
import com.dailyquest.helper.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findByUser(User user);

    List<Quest> findByGame(Game game);

    List<Quest> findByType(QuestType type);

    List<Quest> findByGameAndType(Game game, QuestType type);

    List<Quest> findByUserAndGameOrderByIdAsc(User user, Game game);

    List<Quest> findByUserAndGameAndType(User user, Game game, QuestType type);

    Optional<Quest> findByIdAndUser(Long id, User user);
}