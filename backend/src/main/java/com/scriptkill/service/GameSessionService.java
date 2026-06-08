package com.scriptkill.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.BusinessException;
import com.scriptkill.entity.GameSession;
import com.scriptkill.entity.Player;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.SysAdmin;
import com.scriptkill.mapper.GameSessionMapper;
import com.scriptkill.vo.GameSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameSessionService extends ServiceImpl<GameSessionMapper, GameSession> {

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private SysAdminService sysAdminService;

    @Autowired
    private PlayerService playerService;

    public GameSession createSession(Long scriptId, Long directorId) {
        Script script = scriptService.getById(scriptId);
        if (script == null) {
            throw new BusinessException("剧本不存在");
        }
        GameSession session = new GameSession();
        session.setScriptId(scriptId);
        session.setDirectorId(directorId);
        session.setSessionCode(generateSessionCode());
        session.setStatus(0);
        session.setCurrentStage("intro");
        save(session);
        return session;
    }

    private String generateSessionCode() {
        String code;
        do {
            code = RandomUtil.randomStringUpper(6);
        } while (baseMapper.selectBySessionCode(code) != null);
        return code;
    }

    public GameSession getBySessionCode(String sessionCode) {
        return baseMapper.selectBySessionCode(sessionCode);
    }

    public Page<GameSessionVO> getPage(Long current, Long size, Long directorId) {
        Page<GameSession> page = new Page<>(current, size);
        QueryWrapper<GameSession> wrapper = new QueryWrapper<>();
        if (directorId != null) {
            wrapper.eq("director_id", directorId);
        }
        wrapper.orderByDesc("create_time");
        Page<GameSession> sessionPage = page(page, wrapper);
        Page<GameSessionVO> voPage = new Page<>(sessionPage.getCurrent(), sessionPage.getSize(), sessionPage.getTotal());
        List<GameSessionVO> voList = new ArrayList<>();
        for (GameSession session : sessionPage.getRecords()) {
            GameSessionVO vo = convertToVO(session);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    private GameSessionVO convertToVO(GameSession session) {
        GameSessionVO vo = new GameSessionVO();
        BeanUtils.copyProperties(session, vo);
        Script script = scriptService.getById(session.getScriptId());
        if (script != null) {
            vo.setScriptTitle(script.getTitle());
        }
        SysAdmin admin = sysAdminService.getById(session.getDirectorId());
        if (admin != null) {
            vo.setDirectorName(admin.getNickname());
        }
        List<Player> players = playerService.getBySessionId(session.getId());
        vo.setPlayerCount(players != null ? players.size() : 0);
        return vo;
    }

    public GameSessionVO getDetail(Long id) {
        GameSession session = getById(id);
        if (session == null) {
            return null;
        }
        return convertToVO(session);
    }

    public boolean startSession(Long sessionId) {
        GameSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        if (session.getStatus() != 0) {
            throw new BusinessException("当前状态不允许开始游戏");
        }
        session.setStatus(1);
        session.setStartTime(LocalDateTime.now());
        session.setCurrentStage("playing");
        return updateById(session);
    }

    public boolean endSession(Long sessionId) {
        GameSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        session.setStatus(2);
        session.setEndTime(LocalDateTime.now());
        return updateById(session);
    }

    public boolean pauseSession(Long sessionId) {
        GameSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        if (session.getStatus() != 1) {
            throw new BusinessException("只能暂停进行中的游戏");
        }
        session.setStatus(3);
        return updateById(session);
    }

    public boolean resumeSession(Long sessionId) {
        GameSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }
        if (session.getStatus() != 3) {
            throw new BusinessException("只能恢复已暂停的游戏");
        }
        session.setStatus(1);
        return updateById(session);
    }
}
