package com.dailyquest.helper.controller;

import com.dailyquest.helper.auth.ProgressResponse;
import com.dailyquest.helper.auth.QuestOrderRequest;
import com.dailyquest.helper.auth.QuestRequest;
import com.dailyquest.helper.entity.Quest;
import com.dailyquest.helper.service.QuestService;
import com.dailyquest.helper.user.User;
import com.dailyquest.helper.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestService questService;
    private final UserService userService;

    public QuestController(QuestService questService, UserService userService) {
        this.questService = questService;
        this.userService = userService;
    }

    @GetMapping("/game/{gameId}")
    public List<Quest> getQuestsByGame(@PathVariable Long gameId, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.getQuestsByGame(gameId, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 조회 중 오류가 발생했습니다.");
        }
    }

    @PostMapping
    public Quest addQuest(@RequestBody QuestRequest request, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.addQuest(
                    request.getGameId(),
                    request.getContent(),
                    request.getType(),
                    user
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 추가 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{id}")
    public Quest updateQuest(@PathVariable Long id,
                             @RequestBody QuestRequest request,
                             HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.updateQuestContent(id, request.getContent(), user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 수정 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{id}/order")
    public void updateQuestOrder(@PathVariable Long id,
                                 @RequestBody QuestOrderRequest request,
                                 HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            if (request.getSortOrder() == null) {
                throw new IllegalArgumentException("변경할 순서가 필요합니다.");
            }
            questService.updateQuestOrder(id, request.getSortOrder(), user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 순서 변경 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{id}/toggle")
    public Quest toggleQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.toggleQuest(id, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 상태 변경 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{id}")
    public void deleteQuest(@PathVariable Long id, HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            questService.deleteQuest(id, user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "퀘스트 삭제 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/progress")
    public ProgressResponse getProgress(HttpSession session) {
        User user = userService.getLoginUser(session);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return questService.calculateProgress(user);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "진행도 조회 중 오류가 발생했습니다.");
        }
    }
}
