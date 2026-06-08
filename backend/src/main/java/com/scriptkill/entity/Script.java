package com.scriptkill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("script")
public class Script implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String coverImage;

    private String description;

    private Integer difficulty;

    private Integer duration;

    private Integer playerCountMin;

    private Integer playerCountMax;

    private String author;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
