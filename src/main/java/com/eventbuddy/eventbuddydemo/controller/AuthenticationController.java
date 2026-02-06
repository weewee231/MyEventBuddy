package com.eventbuddy.eventbuddydemo.controller;

import com.eventbuddy.eventbuddydemo.dto.auth.*;
import com.eventbuddy.eventbuddydemo.dto.user.UserDto;
import com.eventbuddy.eventbuddydemo.service.AuthenticationService;
import com.eventbuddy.eventbuddydemo.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Аутентификация", description = "Регистрация, вход, подтверждение email и восстановление доступа")
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    @Operation(summary = "Регистрация пользователя", description = "Создаёт пользователя и отправляет код подтверждения на email.")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("POST /auth/signup - registration attempt for: {}", registerUserDto.getEmail());
        UserDto registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход", description = "Проверяет логин/пароль, возвращает access token и устанавливает refresh token в cookie.")
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
    @Operation(summary = "Подтверждение email", description = "Проверяет код подтверждения и активирует аккаунт.")
    public ResponseEntity<VerifyResponse> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        log.info("POST /auth/verify - verification attempt for: {}", verifyUserDto.getEmail());
        VerifyResponse verifyResponse = authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(verifyResponse);
    }

    @RequestMapping(value = "/auto-login", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Автологин по коду", description = "Выполняет вход по коду из письма и выдаёт токены.")
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
    @Operation(summary = "Обновление access token", description = "Выдаёт новый access token по refresh token из cookie.")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        log.info("POST /auth/refresh - refresh token attempt");
        AuthResponse authResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/resend")
    @Operation(summary = "Повторная отправка кода подтверждения", description = "Отправляет новый код подтверждения на email.")
    public ResponseEntity<Map<String, String>> resendVerificationCode(@RequestParam String email) {
        log.info("POST /auth/resend - resend verification code for: {}", email);
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok(Map.of("message", "Код подтверждения отправлен"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход", description = "Инвалидирует refresh token и очищает cookie.")
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
    @Operation(summary = "Запрос восстановления пароля", description = "Отправляет код восстановления на email.")
    public ResponseEntity<Map<String, String>> requestPasswordRecovery(@Valid @RequestBody RecoveryRequestDto recoveryRequestDto) {
        log.info("POST /auth/recovery - password recovery request for: {}", recoveryRequestDto.getEmail());
        authenticationService.requestPasswordRecovery(recoveryRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код восстановления отправлен на email"));
    }

    @PostMapping("/recovery/resend")
    @Operation(summary = "Повторная отправка кода восстановления", description = "Отправляет новый код восстановления на email.")
    public ResponseEntity<Map<String, String>> resendRecoveryCode(@Valid @RequestBody RecoveryRequestDto recoveryRequestDto) {
        log.info("POST /auth/recovery/resend - resend recovery code for: {}", recoveryRequestDto.getEmail());
        authenticationService.resendRecoveryCode(recoveryRequestDto.getEmail());
        return ResponseEntity.ok(Map.of("message", "Код восстановления отправлен повторно"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Сброс пароля", description = "Проверяет код восстановления и меняет пароль.")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        log.info("POST /auth/reset-password - password reset attempt for: {}", passwordResetDto.getEmail());
        authenticationService.resetPassword(passwordResetDto);
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
    }
}
