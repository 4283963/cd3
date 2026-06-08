package com.scriptkill.dto;

import java.io.Serializable;

public class UnlockClueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clueId;

    private String password;

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
