package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("clue")
public class Clue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scriptId;

    private Long parentId;

    private String name;

    private Integer type;

    private String content;

    private String resourceUrl;

    private String unlockPassword;

    private String unlockHint;

    private String targetRoleIds;

    private Integer isPublic;

    private Integer level;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
