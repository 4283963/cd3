package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "player_progress", autoResultMap = true)
public class PlayerProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playerId;

    private Long sessionId;

    private String currentNode;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> progressData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
