package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("player")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long roleId;

    private String nickname;

    private String avatar;

    private Integer isOnline;

    private LocalDateTime lastActiveTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
