package com.scriptkill.dto;

import java.io.Serializable;

public class WsMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;

    private T data;

    private Long timestamp;

    public WsMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public WsMessage(String type, T data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> WsMessage<T> of(String type, T data) {
        return new WsMessage<>(type, data);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
