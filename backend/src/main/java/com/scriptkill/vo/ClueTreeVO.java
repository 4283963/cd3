package com.scriptkill.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ClueTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long scriptId;

    private Long parentId;

    private String name;

    private Integer type;

    private String content;

    private String resourceUrl;

    private String unlockPassword;

    private String unlockHint;

    private Integer isPublic;

    private Integer level;

    private Integer sortOrder;

    private List<ClueTreeVO> children;
}
