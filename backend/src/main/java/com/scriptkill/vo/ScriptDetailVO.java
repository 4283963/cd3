package com.scriptkill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScriptDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private List<ScriptRoleVO> roles;
}
