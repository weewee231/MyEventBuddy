package com.eventbuddy.eventbuddydemo.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyResponse {
    private String token;

    public VerifyResponse(String token) {
        this.token = token;
    }
}
