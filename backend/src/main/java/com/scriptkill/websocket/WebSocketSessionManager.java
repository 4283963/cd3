package com.scriptkill.websocket;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scriptkill.dto.WsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> sessionRooms = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> sessionRoleMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> lastHeartbeat = new ConcurrentHashMap<>();

    public void addSession(String roomId, String userId, String role, Session session) {
        if (StrUtil.isBlank(roomId) || session == null) {
            return;
        }
        CopyOnWriteArraySet<Session> sessions = sessionRooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
        sessions.add(session);
        sessionRoomMap.put(session.getId(), roomId);
        sessionUserMap.put(session.getId(), userId);
        sessionRoleMap.put(session.getId(), role);
        lastHeartbeat.put(session.getId(), System.currentTimeMillis());
        log.info("[WebSocket] 连接加入房间: roomId={}, userId={}, role={}, 当前房间人数={}",
                roomId, userId, role, sessions.size());
    }

    public void removeSession(Session session) {
        if (session == null) {
            return;
        }
        String sessionId = session.getId();
        String roomId = sessionRoomMap.remove(sessionId);
        String userId = sessionUserMap.remove(sessionId);
        sessionRoleMap.remove(sessionId);
        lastHeartbeat.remove(sessionId);

        if (roomId != null) {
            CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionRooms.remove(roomId);
                    log.info("[WebSocket] 房间已空并清理: roomId={}", roomId);
                } else {
                    log.info("[WebSocket] 连接离开房间: roomId={}, userId={}, 剩余人数={}",
                            roomId, userId, sessions.size());
                }
            }
        }
    }

    public void heartbeat(Session session) {
        if (session != null) {
            lastHeartbeat.put(session.getId(), System.currentTimeMillis());
        }
    }

    public void sendToUser(String roomId, String userId, WsMessage<?> message) {
        if (StrUtil.isBlank(roomId) || StrUtil.isBlank(userId) || message == null) {
            return;
        }
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String json = toJson(message);
        for (Session session : sessions) {
            String uid = sessionUserMap.get(session.getId());
            if (userId.equals(uid)) {
                sendMessage(session, json);
            }
        }
    }

    public void sendToRoom(String roomId, WsMessage<?> message) {
        if (StrUtil.isBlank(roomId) || message == null) {
            return;
        }
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("[WebSocket] 房间无连接: roomId={}", roomId);
            return;
        }
        String json = toJson(message);
        int count = 0;
        for (Session session : sessions) {
            if (sendMessage(session, json)) {
                count++;
            }
        }
        log.info("[WebSocket] 房间广播: roomId={}, type={}, 成功发送={}/{}",
                roomId, message.getType(), count, sessions.size());
    }

    public void sendToRole(String roomId, String role, WsMessage<?> message) {
        if (StrUtil.isBlank(roomId) || StrUtil.isBlank(role) || message == null) {
            return;
        }
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String json = toJson(message);
        for (Session session : sessions) {
            String r = sessionRoleMap.get(session.getId());
            if (role.equals(r)) {
                sendMessage(session, json);
            }
        }
    }

    public void sendToPlayers(String roomId, WsMessage<?> message) {
        sendToRole(roomId, "player", message);
    }

    public void sendToDirectors(String roomId, WsMessage<?> message) {
        sendToRole(roomId, "director", message);
    }

    private boolean sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.getBasicRemote().sendText(message);
            return true;
        } catch (IOException e) {
            log.warn("[WebSocket] 发送消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
            return false;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("[WebSocket] JSON序列化失败", e);
            return "{}";
        }
    }

    public int getOnlineCount(String roomId) {
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        return sessions != null ? sessions.size() : 0;
    }

    public int getPlayerOnlineCount(String roomId) {
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Session s : sessions) {
            String role = sessionRoleMap.get(s.getId());
            if ("player".equals(role)) {
                count++;
            }
        }
        return count;
    }

    public boolean isUserOnline(String roomId, String userId) {
        CopyOnWriteArraySet<Session> sessions = sessionRooms.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        for (Session s : sessions) {
            String uid = sessionUserMap.get(s.getId());
            if (userId.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    public String getRoomId(Session session) {
        return sessionRoomMap.get(session.getId());
    }

    public String getUserId(Session session) {
        return sessionUserMap.get(session.getId());
    }

    public String getRole(Session session) {
        return sessionRoleMap.get(session.getId());
    }
}
