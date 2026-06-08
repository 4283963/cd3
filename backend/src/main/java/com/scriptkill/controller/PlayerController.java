package com.scriptkill.controller;

import com.scriptkill.common.BusinessException;
import com.scriptkill.common.JwtUtil;
import com.scriptkill.common.Result;
import com.scriptkill.dto.PlayerJoinDTO;
import com.scriptkill.dto.UnlockClueDTO;
import com.scriptkill.entity.*;
import com.scriptkill.service.*;
import com.scriptkill.vo.PlayerClueVO;
import com.scriptkill.vo.ScriptDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ScriptRoleService scriptRoleService;

    @Autowired
    private PlayerClueService playerClueService;

    @Autowired
    private PlayerProgressService playerProgressService;

    @Autowired
    private DirectorMessageService directorMessageService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentPlayerId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new BusinessException(401, "请先加入游戏");
        }
        return userId;
    }

    @PostMapping("/join")
    public Result<Map<String, Object>> join(@RequestBody PlayerJoinDTO dto) {
        GameSession session = gameSessionService.getBySessionCode(dto.getSessionCode());
        if (session == null) {
            throw new BusinessException("房间码无效");
        }
        if (session.getStatus() == 2) {
            throw new BusinessException("游戏已结束");
        }
        Player player = playerService.joinGame(session.getId(), dto.getNickname(), dto.getAvatar());
        playerClueService.initPublicCluesForPlayer(player.getId(), session.getId(), session.getScriptId());
        playerProgressService.initProgress(player.getId(), session.getId());
        String token = jwtUtil.generateToken(player.getId(), dto.getNickname(), "player");
        Map<String, Object> result = new HashMap<>();
        result.put("playerId", player.getId());
        result.put("nickname", player.getNickname());
        result.put("sessionId", session.getId());
        result.put("sessionCode", session.getSessionCode());
        result.put("token", token);
        return Result.success(result);
    }

    @GetMapping("/session/info")
    public Result<Map<String, Object>> sessionInfo(HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        if (player == null) {
            throw new BusinessException("玩家不存在");
        }
        GameSession session = gameSessionService.getById(player.getSessionId());
        ScriptDetailVO script = scriptService.getDetail(session.getScriptId());
        ScriptRole role = null;
        if (player.getRoleId() != null) {
            role = scriptRoleService.getById(player.getRoleId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("player", player);
        result.put("session", session);
        result.put("script", script);
        result.put("role", role);
        return Result.success(result);
    }

    @GetMapping("/script")
    public Result<ScriptRole> getMyScript(HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        if (player.getRoleId() == null) {
            throw new BusinessException("角色尚未分配");
        }
        ScriptRole role = scriptRoleService.getById(player.getRoleId());
        return Result.success(role);
    }

    @GetMapping("/clues")
    public Result<List<PlayerClueVO>> getClues(@RequestParam(required = false, defaultValue = "0") Long parentId,
                                                HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        List<PlayerClueVO> clues = playerClueService.getPlayerClueTree(playerId, player.getSessionId(), parentId);
        return Result.success(clues);
    }

    @GetMapping("/clue/{clueId}")
    public Result<PlayerClueVO> getClueDetail(@PathVariable Long clueId, HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        PlayerClueVO vo = playerClueService.getPlayerClueDetail(playerId, clueId);
        return Result.success(vo);
    }

    @PostMapping("/clue/unlock")
    public Result<Map<String, Object>> unlockClue(@RequestBody UnlockClueDTO dto, HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        boolean success = playerClueService.unlockClue(playerId, dto.getClueId(), dto.getPassword());
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (success) {
            PlayerClueVO clue = playerClueService.getPlayerClueDetail(playerId, dto.getClueId());
            result.put("clue", clue);
        }
        return Result.success(result);
    }

    @GetMapping("/messages")
    public Result<List<DirectorMessage>> getMessages(HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        List<DirectorMessage> messages = directorMessageService.getPlayerMessages(player.getSessionId(), playerId);
        return Result.success(messages);
    }

    @GetMapping("/progress")
    public Result<PlayerProgress> getProgress(HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        PlayerProgress progress = playerProgressService.getProgress(playerId, player.getSessionId());
        return Result.success(progress);
    }

    @PostMapping("/progress")
    public Result<Boolean> updateProgress(@RequestBody PlayerProgress progress, HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        Player player = playerService.getById(playerId);
        boolean result = playerProgressService.updateProgress(
                playerId,
                player.getSessionId(),
                progress.getCurrentNode(),
                progress.getProgressData()
        );
        return Result.success(result);
    }

    @PostMapping("/heartbeat")
    public Result<Boolean> heartbeat(HttpServletRequest request) {
        Long playerId = getCurrentPlayerId(request);
        playerService.heartbeat(playerId);
        return Result.success(true);
    }
}
