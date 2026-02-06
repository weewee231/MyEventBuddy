package com.eventbuddy.eventbuddydemo.dto.project;

import com.eventbuddy.eventbuddydemo.dto.member.MemberDto;
import com.eventbuddy.eventbuddydemo.model.Project;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProjectDto {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Project.ProjectStatus status;
    private List<MemberDto> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectDto(Project project) {
        this.id = project.getId();
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.deadline = project.getDeadline();
        this.status = project.getStatus();
        this.members = project.getMembers() != null
                ? project.getMembers().stream().map(MemberDto::new).collect(Collectors.toList())
                : List.of();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }
}
