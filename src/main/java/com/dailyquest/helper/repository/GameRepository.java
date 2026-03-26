package com.dailyquest.helper.repository;

import com.dailyquest.helper.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}