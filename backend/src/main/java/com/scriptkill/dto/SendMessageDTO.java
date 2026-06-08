package com.scriptkill.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SendMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sessionId;

    private Integer receiverType;

    private List<Long> receiverIds;

    private Integer msgType;

    private String title;

    private String content;
}
