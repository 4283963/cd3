package com.scriptkill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scriptkill.common.BusinessException;
import com.scriptkill.common.Result;
import com.scriptkill.dto.DistributeClueDTO;
import com.scriptkill.dto.SendMessageDTO;
import com.scriptkill.entity.*;
import com.scriptkill.service.*;
import com.scriptkill.vo.GameSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/director/session")
public class DirectorSessionController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerClueService playerClueService;

    @Autowired
    private DirectorMessageService directorMessageService;

    @Autowired
    private ClueService clueService;

    @Autowired
    private com.scriptkill.common.JwtUtil jwtUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    @GetMapping("/page")
    public Result<Page<GameSessionVO>> page(@RequestParam(defaultValue = "1") Long current,
                                             @RequestParam(defaultValue = "10") Long size,
                                             HttpServletRequest request) {
        Long directorId = getCurrentUserId(request);
        Page<GameSessionVO> page = gameSessionService.getPage(current, size, directorId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<GameSessionVO> detail(@PathVariable Long id) {
        GameSessionVO vo = gameSessionService.getDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/create")
    public Result<GameSession> create(@RequestParam Long scriptId, HttpServletRequest request) {
        Long directorId = getCurrentUserId(request);
        GameSession session = gameSessionService.createSession(scriptId, directorId);
        return Result.success(session);
    }

    @PostMapping("/{id}/start")
    public Result<Boolean> start(@PathVariable Long id) {
        boolean result = gameSessionService.startSession(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/end")
    public Result<Boolean> end(@PathVariable Long id) {
        boolean result = gameSessionService.endSession(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/pause")
    public Result<Boolean> pause(@PathVariable Long id) {
        boolean result = gameSessionService.pauseSession(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/resume")
    public Result<Boolean> resume(@PathVariable Long id) {
        boolean result = gameSessionService.resumeSession(id);
        return Result.success(result);
    }

    @GetMapping("/{sessionId}/players")
    public Result<List<Player>> players(@PathVariable Long sessionId) {
        List<Player> players = playerService.getBySessionId(sessionId);
        return Result.success(players);
    }

    @PostMapping("/{sessionId}/player/{playerId}/assign-role")
    public Result<Boolean> assignRole(@PathVariable Long sessionId,
                                       @PathVariable Long playerId,
                                       @RequestParam Long roleId) {
        playerService.assignRole(playerId, roleId);
        return Result.success(true);
    }

    @PostMapping("/distribute-clue")
    public Result<Boolean> distributeClue(@RequestBody DistributeClueDTO dto, HttpServletRequest request) {
        Long directorId = getCurrentUserId(request);
        playerClueService.distributeClue(dto.getSessionId(), dto.getClueId(), dto.getPlayerIds(), directorId);
        if (dto.getSendNotification() != null && dto.getSendNotification()) {
            Clue clue = clueService.getById(dto.getClueId());
            if (clue != null) {
                directorMessageService.sendMessage(
                        dto.getSessionId(),
                        directorId,
                        2,
                        dto.getPlayerIds(),
                        3,
                        "新线索通知",
                        "导演给你发送了一条新线索：" + clue.getName()
                );
            }
        }
        return Result.success(true);
    }

    @PostMapping("/send-message")
    public Result<DirectorMessage> sendMessage(@RequestBody SendMessageDTO dto, HttpServletRequest request) {
        Long directorId = getCurrentUserId(request);
        DirectorMessage msg = directorMessageService.sendMessage(
                dto.getSessionId(),
                directorId,
                dto.getReceiverType(),
                dto.getReceiverIds(),
                dto.getMsgType(),
                dto.getTitle(),
                dto.getContent()
        );
        return Result.success(msg);
    }

    @GetMapping("/{sessionId}/messages")
    public Result<List<DirectorMessage>> messages(@PathVariable Long sessionId) {
        List<DirectorMessage> messages = directorMessageService.getSessionMessages(sessionId);
        return Result.success(messages);
    }
}
