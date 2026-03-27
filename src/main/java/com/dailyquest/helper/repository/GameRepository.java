package com.dailyquest.helper.repository;

import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByUser(User user);

    Optional<Game> findByIdAndUser(Long id, User user);
}