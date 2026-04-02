package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.GameRequest;
import com.dailyquest.helper.entity.Game;
import com.dailyquest.helper.service.QuestService;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final QuestService questService;
    private final UserService userService;

    public GameController(QuestService questService, UserService userService) {
        this.questService = questService;
        this.userService = userService;
    }

    @GetMapping
    public List<Game> getGames(HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.getAllGames(user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게임 목록 조회 중 오류가 발생했습니다.");
        }
    }

    @PostMapping
    public Game addGame(@RequestBody GameRequest request, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.addGame(
                    request.getName(),
                    request.getDailyResetTime(),
                    request.getWeeklyResetTime(),
                    user
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게임 추가 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{gameId}")
    public Game updateGameName(@PathVariable Long gameId,
                               @RequestBody GameRequest request,
                               HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.updateGameName(gameId, request.getName(), user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게임 이름 수정 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{gameId}/reset-times")
    public Game updateResetTimes(@PathVariable Long gameId,
                                 @RequestBody GameRequest request,
                                 HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.updateGameResetTimes(
                    gameId,
                    request.getDailyResetTime(),
                    request.getWeeklyResetTime(),
                    user
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "초기화 시간 저장 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{gameId}")
    public void deleteGame(@PathVariable Long gameId, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            questService.deleteGame(gameId, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게임 삭제 중 오류가 발생했습니다.");
        }
    }
}
