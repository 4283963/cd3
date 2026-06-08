package com.scriptkill.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.BusinessException;
import com.scriptkill.entity.Clue;
import com.scriptkill.entity.PlayerClue;
import com.scriptkill.mapper.PlayerClueMapper;
import com.scriptkill.vo.PlayerClueVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerClueService extends ServiceImpl<PlayerClueMapper, PlayerClue> {

    private static final Logger log = LoggerFactory.getLogger(PlayerClueService.class);

    @Autowired
    private ClueService clueService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    public List<PlayerClue> getPlayerClues(Long playerId, Long sessionId) {
        return baseMapper.selectByPlayerIdAndSessionId(playerId, sessionId);
    }

    public List<PlayerClueVO> getPlayerClueTree(Long playerId, Long sessionId, Long parentId) {
        List<PlayerClue> playerClues;
        if (parentId == null || parentId == 0) {
            playerClues = baseMapper.selectByPlayerIdAndSessionId(playerId, sessionId);
        } else {
            playerClues = baseMapper.selectByPlayerIdAndParentId(playerId, sessionId, parentId);
        }
        List<PlayerClueVO> result = new ArrayList<>();
        for (PlayerClue pc : playerClues) {
            Clue clue = clueService.getById(pc.getClueId());
            if (clue == null) {
                continue;
            }
            PlayerClueVO vo = new PlayerClueVO();
            vo.setId(pc.getId());
            vo.setClueId(pc.getClueId());
            vo.setName(clue.getName());
            vo.setType(clue.getType());
            vo.setLevel(clue.getLevel());
            vo.setParentId(clue.getParentId());
            vo.setIsUnlocked(pc.getIsUnlocked());
            vo.setHasChildren(clueService.hasChildren(pc.getClueId()));
            if (pc.getIsUnlocked() == 1) {
                vo.setContent(clue.getContent());
                vo.setResourceUrl(clue.getResourceUrl());
            } else {
                vo.setUnlockHint(clue.getUnlockHint());
            }
            result.add(vo);
        }
        return result;
    }

    public PlayerClueVO getPlayerClueDetail(Long playerId, Long clueId) {
        QueryWrapper<PlayerClue> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId);
        wrapper.eq("clue_id", clueId);
        PlayerClue pc = getOne(wrapper);
        if (pc == null) {
            throw new BusinessException("线索不存在或未获得");
        }
        Clue clue = clueService.getById(clueId);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        PlayerClueVO vo = new PlayerClueVO();
        vo.setId(pc.getId());
        vo.setClueId(pc.getClueId());
        vo.setName(clue.getName());
        vo.setType(clue.getType());
        vo.setLevel(clue.getLevel());
        vo.setParentId(clue.getParentId());
        vo.setIsUnlocked(pc.getIsUnlocked());
        vo.setHasChildren(clueService.hasChildren(clueId));
        if (pc.getIsUnlocked() == 1) {
            vo.setContent(clue.getContent());
            vo.setResourceUrl(clue.getResourceUrl());
        } else {
            vo.setUnlockHint(clue.getUnlockHint());
        }
        return vo;
    }

    public boolean unlockClue(Long playerId, Long clueId, String password) {
        QueryWrapper<PlayerClue> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId);
        wrapper.eq("clue_id", clueId);
        PlayerClue pc = getOne(wrapper);
        if (pc == null) {
            throw new BusinessException("线索不存在或未获得");
        }
        if (pc.getIsUnlocked() == 1) {
            return true;
        }
        Clue clue = clueService.getById(clueId);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        boolean unlocked = false;
        if (clue.getUnlockPassword() == null || clue.getUnlockPassword().isEmpty()) {
            pc.setIsUnlocked(1);
            pc.setUnlockTime(LocalDateTime.now());
            updateById(pc);
            unlocked = true;
        } else if (clue.getUnlockPassword().equals(password)) {
            pc.setIsUnlocked(1);
            pc.setUnlockTime(LocalDateTime.now());
            updateById(pc);
            unlocked = true;
        }
        if (unlocked) {
            try {
                webSocketPushService.pushClueUnlocked(pc.getSessionId(), playerId, clueId);
            } catch (Exception e) {
                log.warn("线索解锁WebSocket推送失败: playerId={}, clueId={}", playerId, clueId, e);
            }
        }
        return unlocked;
    }

    public void distributeClue(Long sessionId, Long clueId, List<Long> playerIds, Long directorId) {
        List<Long> newPlayers = new ArrayList<>();
        for (Long playerId : playerIds) {
            QueryWrapper<PlayerClue> wrapper = new QueryWrapper<>();
            wrapper.eq("player_id", playerId);
            wrapper.eq("clue_id", clueId);
            PlayerClue exist = getOne(wrapper);
            if (exist == null) {
                PlayerClue pc = new PlayerClue();
                pc.setPlayerId(playerId);
                pc.setClueId(clueId);
                pc.setSessionId(sessionId);
                pc.setIsUnlocked(0);
                pc.setDistributedBy(directorId);
                pc.setDistributeTime(LocalDateTime.now());
                save(pc);
                newPlayers.add(playerId);
                try {
                    webSocketPushService.pushNewClue(sessionId, java.util.Collections.singletonList(playerId), pc);
                } catch (Exception e) {
                    log.warn("分发线索WebSocket推送失败: playerId={}, clueId={}", playerId, clueId, e);
                }
            }
        }
    }

    public void initPublicCluesForPlayer(Long playerId, Long sessionId, Long scriptId) {
        List<Clue> publicClues = clueService.getPublicClues(scriptId);
        for (Clue clue : publicClues) {
            QueryWrapper<PlayerClue> wrapper = new QueryWrapper<>();
            wrapper.eq("player_id", playerId);
            wrapper.eq("clue_id", clue.getId());
            PlayerClue exist = getOne(wrapper);
            if (exist == null) {
                PlayerClue pc = new PlayerClue();
                pc.setPlayerId(playerId);
                pc.setClueId(clue.getId());
                pc.setSessionId(sessionId);
                pc.setIsUnlocked(clue.getUnlockPassword() == null || clue.getUnlockPassword().isEmpty() ? 1 : 0);
                if (pc.getIsUnlocked() == 1) {
                    pc.setUnlockTime(LocalDateTime.now());
                }
                save(pc);
            }
        }
    }
}
