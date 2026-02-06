package com.eventbuddy.eventbuddydemo.dto.auth;

import com.eventbuddy.eventbuddydemo.dto.user.UserDto;
import com.eventbuddy.eventbuddydemo.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private UserDto user;
    private String accessToken;
    
    @JsonIgnore
    private String refreshToken;

    public AuthResponse(User user, String accessToken) {
        this.user = new UserDto(user);
        this.accessToken = accessToken;
        this.refreshToken = user.getRefreshToken();
    }
}
