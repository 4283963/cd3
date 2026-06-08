package com.scriptkill.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnlockClueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clueId;

    private String password;
}
