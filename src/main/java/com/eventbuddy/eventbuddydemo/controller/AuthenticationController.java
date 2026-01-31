package com.eventbuddy.eventbuddydemo.controller;

import com.eventbuddy.eventbuddydemo.dto.*;
import com.eventbuddy.eventbuddydemo.dto.AuthResponse;
import com.eventbuddy.eventbuddydemo.dto.VerifyResponse;
import com.eventbuddy.eventbuddydemo.service.AuthenticationService;
import com.eventbuddy.eventbuddydemo.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("POST /auth/signup - registration attempt for: {}", registerUserDto.getEmail());
        UserDto registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        log.info("POST /auth/login - login attempt for: {}", loginUserDto.getEmail());
        
        AuthResponse authResponse = authenticationService.authenticate(loginUserDto);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(30 * 24 * 60 * 60)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        log.info("POST /auth/verify - verification attempt for: {}", verifyUserDto.getEmail());
        VerifyResponse verifyResponse = authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(verifyResponse);
    }

    @RequestMapping(value = "/auto-login", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<AuthResponse> autoLogin(@RequestParam String token) {
        log.info("POST /auth/auto-login - auto-login attempt");
        
        AuthResponse authResponse = authenticationService.processAutoLogin(token);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(30 * 24 * 60 * 60)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("POST /auth/refresh - refresh token attempt");
        AuthResponse authResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/resend")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@RequestParam String email) {
        log.info("POST /auth/resend - resend verification code for: {}", email);
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok(Map.of("message", "Код подтверждения отправлен"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("POST /auth/logout - logout attempt");
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                String userEmail = jwtService.extractUsername(refreshToken);
                if (userEmail != null) {
                    authenticationService.logout(userEmail);
                    log.info("Logout successful for user: {}", userEmail);
                }
            } catch (Exception e) {
                log.warn("Logout error extracting user from refresh token: {}", e.getMessage());
            }
        }

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of("message", "Успешный выход из системы"));
    }

    @PostMapping("/recovery")
    public ResponseEntity<Map<String, String>> requestPasswordRecovery(@Valid @RequestBody RecoveryRequestDto recoveryRequestDto) {
        log.info("POST /auth/recovery - password recovery request for: {}", recoveryRequestDto.getEmail());
        authenticationService.requestPasswordRecovery(recoveryRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код восстановления отправлен на email"));
    }

    @PostMapping("/recovery/resend")
    public ResponseEntity<Map<String, String>> resendRecoveryCode(@Valid @RequestBody RecoveryRequestDto recoveryRequestDto) {
        log.info("POST /auth/recovery/resend - resend recovery code for: {}", recoveryRequestDto.getEmail());
        authenticationService.resendRecoveryCode(recoveryRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код восстановления отправлен повторно"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        log.info("POST /auth/reset-password - password reset attempt for: {}", passwordResetDto.getEmail());
        authenticationService.resetPassword(passwordResetDto);
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
    }
}
