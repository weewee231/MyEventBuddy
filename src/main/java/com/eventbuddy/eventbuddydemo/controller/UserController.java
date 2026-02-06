package com.eventbuddy.eventbuddydemo.controller;

import com.eventbuddy.eventbuddydemo.dto.user.UpdateUserDto;
import com.eventbuddy.eventbuddydemo.dto.user.UserDto;
import com.eventbuddy.eventbuddydemo.model.User;
import com.eventbuddy.eventbuddydemo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Профиль пользователя и аватар")
public class UserController {
    private final UserService userService;

    @GetMapping("/users/me")
    @Operation(summary = "Мой профиль", description = "Возвращает профиль текущего пользователя.")
    public ResponseEntity<UserDto> authenticatedUser() {
        User currentUser = getCurrentUser();
        log.info("GET /users/me - getting profile for user: {}", currentUser.getEmail());
        return ResponseEntity.ok(new UserDto(currentUser));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Обновить профиль", description = "Обновляет профиль текущего пользователя.")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateUserDto dto) {
        User currentUser = getCurrentUser();
        log.info("PUT /users/me - updating profile for user: {}", currentUser.getEmail());
        UserDto updatedUser = userService.updateProfile(currentUser, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/")
    @Operation(summary = "Список пользователей", description = "Возвращает всех пользователей (требует ограничения доступа).")
    public ResponseEntity<List<UserDto>> allUsers() {
        log.info("GET /users/ - getting all users");
        List<UserDto> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user/avatar")
    @Operation(summary = "Загрузить аватар", description = "Загружает аватар пользователя.")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        User currentUser = getCurrentUser();
        log.info("POST /user/avatar - uploading avatar for user: {}", currentUser.getEmail());
        String avatarUrl = userService.uploadAvatar(currentUser, file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @DeleteMapping("/users/me")
    @Operation(summary = "Удалить аккаунт", description = "Удаляет аккаунт текущего пользователя.")
    public ResponseEntity<Map<String, String>> deleteCurrentUser() {
        User currentUser = getCurrentUser();
        log.info("DELETE /users/me - deleting account for user: {}", currentUser.getEmail());
        userService.deleteCurrentUser(currentUser);
        return ResponseEntity.ok(Map.of("message", "Аккаунт успешно удален"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
