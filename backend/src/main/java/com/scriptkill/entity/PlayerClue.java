package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("player_clue")
public class PlayerClue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playerId;

    private Long clueId;

    private Long sessionId;

    private Integer isUnlocked;

    private LocalDateTime unlockTime;

    private Long distributedBy;

    private LocalDateTime distributeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
