package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Player;
import com.scriptkill.entity.ScriptRole;
import com.scriptkill.mapper.PlayerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlayerService extends ServiceImpl<PlayerMapper, Player> {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private ScriptRoleService scriptRoleService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    public List<Player> getBySessionId(Long sessionId) {
        return baseMapper.selectBySessionId(sessionId);
    }

    public Player joinGame(Long sessionId, String nickname, String avatar) {
        Player player = new Player();
        player.setSessionId(sessionId);
        player.setNickname(nickname);
        player.setAvatar(avatar);
        player.setIsOnline(1);
        player.setLastActiveTime(LocalDateTime.now());
        save(player);
        return player;
    }

    public void updateOnlineStatus(Long playerId, boolean isOnline) {
        Player player = getById(playerId);
        if (player != null) {
            player.setIsOnline(isOnline ? 1 : 0);
            if (isOnline) {
                player.setLastActiveTime(LocalDateTime.now());
            }
            updateById(player);
        }
    }

    public void assignRole(Long playerId, Long roleId) {
        Player player = getById(playerId);
        if (player != null) {
            player.setRoleId(roleId);
            updateById(player);

            try {
                ScriptRole role = scriptRoleService.getById(roleId);
                webSocketPushService.pushRoleAssigned(player.getSessionId(), playerId, role);
            } catch (Exception e) {
                log.warn("角色分配WebSocket推送失败: playerId={}, roleId={}", playerId, roleId, e);
            }
        }
    }

    public Player getById(Long id) {
        return baseMapper.selectById(id);
    }

    public void heartbeat(Long playerId) {
        Player player = getById(playerId);
        if (player != null) {
            player.setLastActiveTime(LocalDateTime.now());
            player.setIsOnline(1);
            updateById(player);
        }
    }
}
