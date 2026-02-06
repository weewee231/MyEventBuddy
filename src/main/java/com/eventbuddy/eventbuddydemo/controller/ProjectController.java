package com.eventbuddy.eventbuddydemo.controller;

import com.eventbuddy.eventbuddydemo.dto.member.*;
import com.eventbuddy.eventbuddydemo.dto.project.*;
import com.eventbuddy.eventbuddydemo.model.Project;
import com.eventbuddy.eventbuddydemo.model.User;
import com.eventbuddy.eventbuddydemo.service.ProjectMemberService;
import com.eventbuddy.eventbuddydemo.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Проекты", description = "Управление проектами и участниками")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectMemberService memberService;

    @GetMapping
    @Operation(summary = "Список проектов", description = "Возвращает все проекты текущего пользователя.")
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        User currentUser = getCurrentUser();
        log.info("GET /projects - getting all projects for user: {}", currentUser.getEmail());
        List<ProjectDto> projects = projectService.getAllProjects(currentUser);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск проектов", description = "Поиск и фильтрация проектов по параметрам.")
    public ResponseEntity<List<ProjectDto>> searchProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Project.ProjectStatus status,
            @RequestParam(required = false) LocalDateTime deadlineFrom,
            @RequestParam(required = false) LocalDateTime deadlineTo,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection
    ) {
        User currentUser = getCurrentUser();
        log.info("GET /projects/search - searching projects for user: {} with search={}, status={}", 
                currentUser.getEmail(), search, status);
        
        ProjectFilterDto filter = new ProjectFilterDto();
        filter.setSearch(search);
        filter.setStatus(status);
        filter.setDeadlineFrom(deadlineFrom);
        filter.setDeadlineTo(deadlineTo);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        
        List<ProjectDto> projects = projectService.searchProjects(currentUser, filter);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Проект по ID", description = "Возвращает проект по идентификатору, если есть доступ.")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        log.info("GET /projects/{} - getting project for user: {}", id, currentUser.getEmail());
        ProjectDto project = projectService.getProject(id, currentUser);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    @Operation(summary = "Создать проект", description = "Создаёт новый проект для текущего пользователя.")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectDto dto) {
        User currentUser = getCurrentUser();
        log.info("POST /projects - creating project for user: {}", currentUser.getEmail());
        ProjectDto project = projectService.createProject(dto, currentUser);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить проект", description = "Обновляет данные проекта по идентификатору.")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody EditProjectDto dto
    ) {
        User currentUser = getCurrentUser();
        log.info("PUT /projects/{} - updating project for user: {}", id, currentUser.getEmail());
        ProjectDto project = projectService.updateProject(id, dto, currentUser);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить проект", description = "Удаляет проект по идентификатору.")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        log.info("DELETE /projects/{} - deleting project for user: {}", id, currentUser.getEmail());
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Проект успешно удален"));
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "Список участников", description = "Возвращает участников проекта с поиском и сортировкой.")
    public ResponseEntity<List<MemberDto>> getMembers(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "name") String sort
    ) {
        User currentUser = getCurrentUser();
        log.info("GET /projects/{}/members - search={}, sort={}", projectId, search, sort);
        List<MemberDto> members = memberService.getMembers(projectId, currentUser, search, sort);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{projectId}/members")
    @Operation(summary = "Пригласить участника", description = "Добавляет пользователя в проект по email.")
    public ResponseEntity<MemberDto> inviteMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody InviteMemberDto dto
    ) {
        User currentUser = getCurrentUser();
        log.info("POST /projects/{}/members - inviting {}", projectId, dto.getEmail());
        MemberDto member = memberService.inviteMember(projectId, dto, currentUser);
        return ResponseEntity.ok(member);
    }

    @PatchMapping("/{projectId}/members")
    @Operation(summary = "Обновить участника", description = "Меняет роль или тег участника проекта.")
    public ResponseEntity<MemberDto> updateMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateMemberDto dto
    ) {
        User currentUser = getCurrentUser();
        log.info("PATCH /projects/{}/members - updating {}", projectId, dto.getEmail());
        MemberDto member = memberService.updateMember(projectId, dto, currentUser);
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/{projectId}/members")
    @Operation(summary = "Удалить участника", description = "Удаляет участника из проекта.")
    public ResponseEntity<Map<String, String>> deleteMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody DeleteMemberDto dto
    ) {
        User currentUser = getCurrentUser();
        log.info("DELETE /projects/{}/members - deleting {}", projectId, dto.getEmail());
        memberService.deleteMember(projectId, dto, currentUser);
        return ResponseEntity.ok(Map.of("message", "Участник удален из проекта"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
