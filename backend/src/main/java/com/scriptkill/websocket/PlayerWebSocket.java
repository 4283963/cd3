package com.scriptkill.websocket;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptkill.common.JwtUtil;
import com.scriptkill.dto.WsMessage;
import com.scriptkill.entity.GameSession;
import com.scriptkill.entity.Player;
import com.scriptkill.service.GameSessionService;
import com.scriptkill.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@ServerEndpoint("/ws/player/{sessionId}")
public class PlayerWebSocket {

    private static final Logger log = LoggerFactory.getLogger(PlayerWebSocket.class);

    private static WebSocketSessionManager sessionManager;
    private static JwtUtil jwtUtil;
    private static PlayerService playerService;
    private static GameSessionService gameSessionService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setSessionManager(WebSocketSessionManager sessionManager) {
        PlayerWebSocket.sessionManager = sessionManager;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        PlayerWebSocket.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        PlayerWebSocket.playerService = playerService;
    }

    @Autowired
    public void setGameSessionService(GameSessionService gameSessionService) {
        PlayerWebSocket.gameSessionService = gameSessionService;
    }

    @OnOpen
    public void onOpen(@PathParam("sessionId") String sessionIdStr, Session session) {
        String token = getTokenFromSession(session);
        if (StrUtil.isBlank(token)) {
            closeSession(session, "未提供Token");
            return;
        }

        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        if (userId == null || !"player".equals(role)) {
            closeSession(session, "Token无效或无权限");
            return;
        }

        Long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdStr);
        } catch (NumberFormatException e) {
            closeSession(session, "场次ID无效");
            return;
        }

        GameSession gameSession = gameSessionService.getById(sessionId);
        if (gameSession == null) {
            closeSession(session, "场次不存在");
            return;
        }

        Player player = playerService.getById(userId);
        if (player == null || !player.getSessionId().equals(sessionId)) {
            closeSession(session, "玩家未加入该场次");
            return;
        }

        String roomId = "session_" + sessionId;
        sessionManager.addSession(roomId, String.valueOf(userId), "player", session);

        playerService.updateOnlineStatus(userId, true);

        WsMessage<String> welcomeMsg = WsMessage.of("connected", "连接成功");
        sendMessage(session, welcomeMsg);

        WsMessage<Map<String, Object>> statusMsg = WsMessage.of("player_status_change",
                Map.of("onlineCount", sessionManager.getPlayerOnlineCount(roomId)));
        sessionManager.sendToDirectors(roomId, statusMsg);

        log.info("[玩家WebSocket] 连接建立: playerId={}, sessionId={}, 房间在线={}",
                userId, sessionId, sessionManager.getPlayerOnlineCount(roomId));
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            WsMessage<?> msg = objectMapper.readValue(message, WsMessage.class);
            if ("heartbeat".equals(msg.getType())) {
                sessionManager.heartbeat(session);
                WsMessage<String> pong = WsMessage.of("pong", "pong");
                sendMessage(session, pong);
            }
        } catch (Exception e) {
            log.warn("[玩家WebSocket] 消息解析失败: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String roomId = sessionManager.getRoomId(session);
        String userId = sessionManager.getUserId(session);

        sessionManager.removeSession(session);

        if (userId != null) {
            try {
                playerService.updateOnlineStatus(Long.parseLong(userId), false);
            } catch (Exception e) {
                log.warn("[玩家WebSocket] 更新在线状态失败: {}", e.getMessage());
            }
        }

        if (roomId != null) {
            WsMessage<Map<String, Object>> statusMsg = WsMessage.of("player_status_change",
                    Map.of("onlineCount", sessionManager.getPlayerOnlineCount(roomId)));
            sessionManager.sendToDirectors(roomId, statusMsg);
        }

        log.info("[玩家WebSocket] 连接关闭: userId={}, reason={}", userId, reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[玩家WebSocket] 连接异常: sessionId={}, error={}", session.getId(), error.getMessage());
    }

    private String getTokenFromSession(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        if (params != null && params.containsKey("token")) {
            List<String> tokens = params.get("token");
            if (tokens != null && !tokens.isEmpty()) {
                return tokens.get(0);
            }
        }
        String query = session.getQueryString();
        if (StrUtil.isNotBlank(query)) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private void sendMessage(Session session, WsMessage<?> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            if (session.isOpen()) {
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            log.warn("[玩家WebSocket] 发送消息失败: {}", e.getMessage());
        }
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            log.warn("[玩家WebSocket] 关闭连接失败: {}", e.getMessage());
        }
    }
}
