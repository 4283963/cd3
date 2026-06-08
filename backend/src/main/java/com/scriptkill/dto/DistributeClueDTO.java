package com.scriptkill.dto;

import java.io.Serializable;
import java.util.List;

public class DistributeClueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;

    private Long clueId;

    private List<Long> playerIds;

    private Boolean sendNotification;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public Boolean getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(Boolean sendNotification) {
        this.sendNotification = sendNotification;
    }
}
