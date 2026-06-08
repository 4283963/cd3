package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.PlayerProgress;
import com.scriptkill.mapper.PlayerProgressMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PlayerProgressService extends ServiceImpl<PlayerProgressMapper, PlayerProgress> {

    public PlayerProgress getProgress(Long playerId, Long sessionId) {
        QueryWrapper<PlayerProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId);
        wrapper.eq("session_id", sessionId);
        return getOne(wrapper);
    }

    public PlayerProgress initProgress(Long playerId, Long sessionId) {
        PlayerProgress progress = new PlayerProgress();
        progress.setPlayerId(playerId);
        progress.setSessionId(sessionId);
        progress.setCurrentNode("start");
        save(progress);
        return progress;
    }

    public boolean updateProgress(Long playerId, Long sessionId, String currentNode, Map<String, Object> progressData) {
        QueryWrapper<PlayerProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId);
        wrapper.eq("session_id", sessionId);
        PlayerProgress progress = getOne(wrapper);
        if (progress == null) {
            progress = new PlayerProgress();
            progress.setPlayerId(playerId);
            progress.setSessionId(sessionId);
            progress.setCurrentNode(currentNode);
            progress.setProgressData(progressData);
            return save(progress);
        }
        if (currentNode != null) {
            progress.setCurrentNode(currentNode);
        }
        if (progressData != null) {
            progress.setProgressData(progressData);
        }
        return updateById(progress);
    }
}
