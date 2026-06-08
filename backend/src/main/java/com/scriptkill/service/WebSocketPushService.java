package com.scriptkill.service;

import com.scriptkill.dto.WsMessage;
import com.scriptkill.entity.DirectorMessage;
import com.scriptkill.entity.PlayerClue;
import com.scriptkill.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketPushService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPushService.class);

    @Autowired
    private WebSocketSessionManager sessionManager;

    public void pushNewClue(Long sessionId, List<Long> playerIds, PlayerClue playerClue) {
        String roomId = "session_" + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("clueId", playerClue.getClueId());
        data.put("playerClueId", playerClue.getId());
        data.put("isUnlocked", playerClue.getIsUnlocked());

        WsMessage<Map<String, Object>> message = WsMessage.of("new_clue", data);

        for (Long playerId : playerIds) {
            sessionManager.sendToUser(roomId, String.valueOf(playerId), message);
        }
        log.info("[WebSocket推送] 新线索: sessionId={}, playerCount={}", sessionId, playerIds.size());
    }

    public void pushNewClueToAll(Long sessionId) {
        String roomId = "session_" + sessionId;
        WsMessage<String> message = WsMessage.of("new_clue_notify", "您收到了新线索");
        sessionManager.sendToPlayers(roomId, message);
    }

    public void pushMessage(Long sessionId, DirectorMessage msg) {
        String roomId = "session_" + sessionId;
        WsMessage<DirectorMessage> message = WsMessage.of("new_message", msg);

        if (msg.getReceiverType() == 1) {
            sessionManager.sendToPlayers(roomId, message);
        } else if (msg.getReceiverType() == 2 && msg.getReceiverIds() != null) {
            String[] ids = msg.getReceiverIds().split(",");
            for (String id : ids) {
                try {
                    sessionManager.sendToUser(roomId, id.trim(), message);
                } catch (Exception e) {
                    log.warn("[WebSocket推送] 消息推送失败: userId={}", id);
                }
            }
        }
        log.info("[WebSocket推送] 新消息: sessionId={}, type={}", sessionId, msg.getMsgType());
    }

    public void pushGameStatusChange(Long sessionId, String status) {
        String roomId = "session_" + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("status", status);
        WsMessage<Map<String, Object>> message = WsMessage.of("game_status_change", data);
        sessionManager.sendToRoom(roomId, message);
        log.info("[WebSocket推送] 游戏状态变更: sessionId={}, status={}", sessionId, status);
    }

    public void pushRoleAssigned(Long sessionId, Long playerId, Object roleInfo) {
        String roomId = "session_" + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("role", roleInfo);
        WsMessage<Map<String, Object>> message = WsMessage.of("role_assigned", data);
        sessionManager.sendToUser(roomId, String.valueOf(playerId), message);
        log.info("[WebSocket推送] 角色分配: sessionId={}, playerId={}", sessionId, playerId);
    }

    public void pushClueUnlocked(Long sessionId, Long playerId, Long clueId) {
        String roomId = "session_" + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("clueId", clueId);
        WsMessage<Map<String, Object>> message = WsMessage.of("clue_unlocked", data);
        sessionManager.sendToDirectors(roomId, message);
        log.info("[WebSocket推送] 线索已解锁: sessionId={}, playerId={}, clueId={}", sessionId, playerId, clueId);
    }

    public void pushPlayerStatusUpdate(Long sessionId) {
        String roomId = "session_" + sessionId;
        Map<String, Object> data = new HashMap<>();
        data.put("onlineCount", sessionManager.getPlayerOnlineCount(roomId));
        WsMessage<Map<String, Object>> message = WsMessage.of("player_status_change", data);
        sessionManager.sendToDirectors(roomId, message);
    }
}
