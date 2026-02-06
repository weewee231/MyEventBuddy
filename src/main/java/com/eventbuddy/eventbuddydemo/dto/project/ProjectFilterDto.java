package com.eventbuddy.eventbuddydemo.dto.project;

import com.eventbuddy.eventbuddydemo.model.Project;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectFilterDto {
    private String search;
    private Project.ProjectStatus status;
    private LocalDateTime deadlineFrom;
    private LocalDateTime deadlineTo;
    private String sortBy;
    private String sortDirection;
}
