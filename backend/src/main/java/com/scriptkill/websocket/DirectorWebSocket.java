package com.scriptkill.websocket;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptkill.common.JwtUtil;
import com.scriptkill.dto.WsMessage;
import com.scriptkill.entity.GameSession;
import com.scriptkill.service.GameSessionService;
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
@ServerEndpoint("/ws/director/{sessionId}")
public class DirectorWebSocket {

    private static final Logger log = LoggerFactory.getLogger(DirectorWebSocket.class);

    private static WebSocketSessionManager sessionManager;
    private static JwtUtil jwtUtil;
    private static GameSessionService gameSessionService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setSessionManager(WebSocketSessionManager sessionManager) {
        DirectorWebSocket.sessionManager = sessionManager;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        DirectorWebSocket.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setGameSessionService(GameSessionService gameSessionService) {
        DirectorWebSocket.gameSessionService = gameSessionService;
    }

    @OnOpen
    public void onOpen(@PathParam("sessionId") String sessionIdStr, Session session, EndpointConfig config) {
        String token = getTokenFromSession(session);
        if (StrUtil.isBlank(token)) {
            closeSession(session, "未提供Token");
            return;
        }
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        if (userId == null || !"admin".equals(role)) {
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
        if (!gameSession.getDirectorId().equals(userId)) {
            closeSession(session, "无权限访问该场次");
            return;
        }

        String roomId = "session_" + sessionId;
        sessionManager.addSession(roomId, String.valueOf(userId), "director", session);

        WsMessage<String> welcomeMsg = WsMessage.of("connected", "连接成功");
        sendMessage(session, welcomeMsg);

        WsMessage<Map<String, Object>> statusMsg = WsMessage.of("player_status_change",
                Map.of("onlineCount", sessionManager.getPlayerOnlineCount(roomId)));
        sendMessage(session, statusMsg);

        log.info("[导演WebSocket] 连接建立成功: userId={}, sessionId={}", userId, sessionId);
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
            log.warn("[导演WebSocket] 消息解析失败: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessionManager.removeSession(session);
        String roomId = sessionManager.getRoomId(session);
        if (roomId != null) {
            WsMessage<Map<String, Object>> statusMsg = WsMessage.of("player_status_change",
                    Map.of("onlineCount", sessionManager.getPlayerOnlineCount(roomId)));
            sessionManager.sendToDirectors(roomId, statusMsg);
        }
        log.info("[导演WebSocket] 连接关闭: {}, reason={}", session.getId(), reason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[导演WebSocket] 连接异常: sessionId={}, error={}", session.getId(), error.getMessage());
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
            log.warn("[导演WebSocket] 发送消息失败: {}", e.getMessage());
        }
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            log.warn("[导演WebSocket] 关闭连接失败: {}", e.getMessage());
        }
    }
}
