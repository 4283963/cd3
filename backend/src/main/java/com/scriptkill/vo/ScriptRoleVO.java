package com.scriptkill.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScriptRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long scriptId;

    private String name;

    private Integer gender;

    private String avatar;

    private String description;

    private String backgroundStory;

    private String secret;

    private Integer sortOrder;
}
