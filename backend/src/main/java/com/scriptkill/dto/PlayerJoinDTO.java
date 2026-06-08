package com.scriptkill.dto;

import java.io.Serializable;

public class PlayerJoinDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionCode;

    private String nickname;

    private String avatar;

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
