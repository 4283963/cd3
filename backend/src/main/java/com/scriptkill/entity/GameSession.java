package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("game_session")
public class GameSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scriptId;

    private String sessionCode;

    private Long directorId;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String currentStage;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
