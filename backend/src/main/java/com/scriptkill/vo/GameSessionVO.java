package com.scriptkill.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class GameSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long scriptId;

    private String scriptTitle;

    private String sessionCode;

    private Long directorId;

    private String directorName;

    private Integer status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String currentStage;

    private Integer playerCount;

    private LocalDateTime createTime;
}
