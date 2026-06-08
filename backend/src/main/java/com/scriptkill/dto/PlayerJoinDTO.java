package com.scriptkill.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlayerJoinDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionCode;

    private String nickname;

    private String avatar;
}
