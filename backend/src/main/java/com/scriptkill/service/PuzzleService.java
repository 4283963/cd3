package com.scriptkill.service;

import cn.hutool.json.JSONUtil;
import com.scriptkill.entity.Clue;
import com.scriptkill.entity.PlayerClue;
import com.scriptkill.mapper.ClueMapper;
import com.scriptkill.mapper.PlayerClueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PuzzleService {

    private static final Logger log = LoggerFactory.getLogger(PuzzleService.class);

    @Autowired
    private PlayerClueMapper playerClueMapper;

    @Autowired
    private ClueMapper clueMapper;

    @Autowired
    private WebSocketPushService webSocketPushService;

    public List<Integer> generatePuzzle(int rows, int cols) {
        int total = rows * cols;
        List<Integer> pieces = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            pieces.add(i);
        }
        do {
            Collections.shuffle(pieces);
        } while (!isSolvable(pieces, rows, cols) || isCompleted(pieces));
        return pieces;
    }

    private boolean isSolvable(List<Integer> pieces, int rows, int cols) {
        int inversions = 0;
        int total = rows * cols;
        int emptyValue = total - 1;
        for (int i = 0; i < total - 1; i++) {
            if (pieces.get(i) == emptyValue) continue;
            for (int j = i + 1; j < total; j++) {
                if (pieces.get(j) == emptyValue) continue;
                if (pieces.get(i) > pieces.get(j)) {
                    inversions++;
                }
            }
        }
        if (cols % 2 == 1) {
            return inversions % 2 == 0;
        } else {
            int emptyRow = pieces.indexOf(emptyValue) / cols;
            int rowFromBottom = rows - emptyRow;
            return (rowFromBottom % 2 == 0) ? (inversions % 2 == 1) : (inversions % 2 == 0);
        }
    }

    private boolean isCompleted(List<Integer> pieces) {
        for (int i = 0; i < pieces.size(); i++) {
            if (!pieces.get(i).equals(i)) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public PlayerClue startPuzzle(Long playerId, Long playerClueId) {
        PlayerClue playerClue = playerClueMapper.selectById(playerClueId);
        if (playerClue == null) {
            throw new RuntimeException("线索不存在");
        }
        if (!playerClue.getPlayerId().equals(playerId)) {
            throw new RuntimeException("无权操作此线索");
        }
        if (playerClue.getPuzzleStatus() != null && playerClue.getPuzzleStatus() == 2) {
            throw new RuntimeException("拼图已完成");
        }
        if (playerClue.getPuzzleStatus() != null && playerClue.getPuzzleStatus() == 3) {
            throw new RuntimeException("线索已销毁");
        }

        Clue clue = clueMapper.selectById(playerClue.getClueId());
        if (clue == null || clue.getIsPuzzle() == null || clue.getIsPuzzle() != 1) {
            throw new RuntimeException("该线索不需要拼图解锁");
        }

        int rows = clue.getPuzzleRows() != null ? clue.getPuzzleRows() : 3;
        int cols = clue.getPuzzleCols() != null ? clue.getPuzzleCols() : 3;

        List<Integer> puzzle = generatePuzzle(rows, cols);

        playerClue.setPuzzleStatus(1);
        playerClue.setPuzzleStartTime(LocalDateTime.now());
        playerClue.setPuzzleCurrent(JSONUtil.toJsonStr(puzzle));
        playerClueMapper.updateById(playerClue);

        log.info("[拼图] 开始拼图: playerId={}, playerClueId={}", playerId, playerClueId);
        return playerClue;
    }

    @Transactional
    public PlayerClue movePuzzle(Long playerId, Long playerClueId, int pieceIndex) {
        PlayerClue playerClue = playerClueMapper.selectById(playerClueId);
        if (playerClue == null) {
            throw new RuntimeException("线索不存在");
        }
        if (!playerClue.getPlayerId().equals(playerId)) {
            throw new RuntimeException("无权操作此线索");
        }
        if (playerClue.getPuzzleStatus() == null || playerClue.getPuzzleStatus() != 1) {
            throw new RuntimeException("拼图未开始或已结束");
        }

        Clue clue = clueMapper.selectById(playerClue.getClueId());
        int timeLimit = clue.getPuzzleTimeLimit() != null ? clue.getPuzzleTimeLimit() : 180;
        int rows = clue.getPuzzleRows() != null ? clue.getPuzzleRows() : 3;
        int cols = clue.getPuzzleCols() != null ? clue.getPuzzleCols() : 3;

        LocalDateTime startTime = playerClue.getPuzzleStartTime();
        if (startTime != null) {
            long secondsElapsed = Duration.between(startTime, LocalDateTime.now()).getSeconds();
            if (secondsElapsed > timeLimit) {
                playerClue.setPuzzleStatus(3);
                playerClueMapper.updateById(playerClue);
                throw new RuntimeException("时间已到，线索已销毁");
            }
        }

        List<Integer> puzzle = JSONUtil.toList(playerClue.getPuzzleCurrent(), Integer.class);
        int emptyValue = rows * cols - 1;
        int emptyIndex = puzzle.indexOf(emptyValue);

        if (!isAdjacent(pieceIndex, emptyIndex, rows, cols)) {
            throw new RuntimeException("该位置无法移动");
        }

        Collections.swap(puzzle, pieceIndex, emptyIndex);

        playerClue.setPuzzleCurrent(JSONUtil.toJsonStr(puzzle));

        if (isCompleted(puzzle)) {
            playerClue.setPuzzleStatus(2);
            playerClue.setIsUnlocked(1);
            playerClue.setUnlockTime(LocalDateTime.now());

            webSocketPushService.pushClueUnlocked(
                playerClue.getSessionId(),
                playerId,
                playerClue.getClueId()
            );

            log.info("[拼图] 拼图完成: playerId={}, playerClueId={}", playerId, playerClueId);
        }

        playerClueMapper.updateById(playerClue);
        return playerClue;
    }

    private boolean isAdjacent(int index1, int index2, int rows, int cols) {
        int r1 = index1 / cols;
        int c1 = index1 % cols;
        int r2 = index2 / cols;
        int c2 = index2 % cols;
        return (Math.abs(r1 - r2) == 1 && c1 == c2) ||
               (Math.abs(c1 - c2) == 1 && r1 == r2);
    }

    public boolean checkTimeout(Long playerClueId) {
        PlayerClue playerClue = playerClueMapper.selectById(playerClueId);
        if (playerClue == null) {
            return false;
        }
        if (playerClue.getPuzzleStatus() == null || playerClue.getPuzzleStatus() != 1) {
            return false;
        }
        if (playerClue.getPuzzleStartTime() == null) {
            return false;
        }

        Clue clue = clueMapper.selectById(playerClue.getClueId());
        int timeLimit = clue.getPuzzleTimeLimit() != null ? clue.getPuzzleTimeLimit() : 180;

        long secondsElapsed = Duration.between(
            playerClue.getPuzzleStartTime(),
            LocalDateTime.now()
        ).getSeconds();

        if (secondsElapsed > timeLimit) {
            playerClue.setPuzzleStatus(3);
            playerClueMapper.updateById(playerClue);
            return true;
        }
        return false;
    }

    public List<Integer> getPuzzleState(Long playerId, Long playerClueId) {
        PlayerClue playerClue = playerClueMapper.selectById(playerClueId);
        if (playerClue == null) {
            throw new RuntimeException("线索不存在");
        }
        if (!playerClue.getPlayerId().equals(playerId)) {
            throw new RuntimeException("无权操作此线索");
        }
        if (playerClue.getPuzzleCurrent() == null) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(playerClue.getPuzzleCurrent(), Integer.class);
    }
}
