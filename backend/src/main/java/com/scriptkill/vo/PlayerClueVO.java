package com.scriptkill.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlayerClueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long clueId;

    private String name;

    private Integer type;

    private String content;

    private String resourceUrl;

    private Integer isUnlocked;

    private String unlockHint;

    private Integer level;

    private Long parentId;

    private Boolean hasChildren;
}
