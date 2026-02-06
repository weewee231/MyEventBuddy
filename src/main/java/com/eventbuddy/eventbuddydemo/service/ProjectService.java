package com.eventbuddy.eventbuddydemo.service;

import com.eventbuddy.eventbuddydemo.dto.project.CreateProjectDto;
import com.eventbuddy.eventbuddydemo.dto.project.EditProjectDto;
import com.eventbuddy.eventbuddydemo.dto.project.ProjectDto;
import com.eventbuddy.eventbuddydemo.dto.project.ProjectFilterDto;
import com.eventbuddy.eventbuddydemo.exception.AuthException;
import com.eventbuddy.eventbuddydemo.model.Project;
import com.eventbuddy.eventbuddydemo.model.User;
import com.eventbuddy.eventbuddydemo.repository.ProjectRepository;
import com.eventbuddy.eventbuddydemo.specification.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAllProjects(User owner) {
        log.info("Getting all projects for user: {}", owner.getEmail());
        return projectRepository.findByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> searchProjects(User owner, ProjectFilterDto filter) {
        log.info("Searching projects for user: {} with filters: search={}, status={}, deadlineFrom={}, deadlineTo={}",
                owner.getEmail(), filter.getSearch(), filter.getStatus(), 
                filter.getDeadlineFrom(), filter.getDeadlineTo());
        
        Specification<Project> spec = ProjectSpecification.withFilters(owner, filter);
        Sort sort = buildSort(filter);
        
        return projectRepository.findAll(spec, sort)
                .stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }

    private Sort buildSort(ProjectFilterDto filter) {
        String sortBy = filter.getSortBy();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        
        if (!List.of("title", "deadline", "createdAt", "status", "updatedAt").contains(sortBy)) {
            sortBy = "createdAt";
        }
        
        Sort.Direction direction = Sort.Direction.DESC;
        if ("ASC".equalsIgnoreCase(filter.getSortDirection())) {
            direction = Sort.Direction.ASC;
        }
        
        return Sort.by(direction, sortBy);
    }

    public ProjectDto getProject(UUID id, User owner) {
        log.info("Getting project {} for user: {}", id, owner.getEmail());
        Project project = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Project not found: {} for user: {}", id, owner.getEmail());
                    return new AuthException("Проект не найден", "id", "Проект с таким ID не найден");
                });
        return new ProjectDto(project);
    }

    @Transactional
    public ProjectDto createProject(CreateProjectDto dto, User owner) {
        log.info("Creating project '{}' for user: {}", dto.getTitle(), owner.getEmail());
        
        Project project = new Project(
                dto.getTitle(),
                dto.getDescription(),
                dto.getDeadline(),
                owner
        );
        
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        
        Project savedProject = projectRepository.saveAndFlush(project);
        log.info("Project created with ID: {} and status: {}", savedProject.getId(), savedProject.getStatus());
        
        return new ProjectDto(savedProject);
    }

    @Transactional
    public ProjectDto updateProject(UUID id, EditProjectDto dto, User owner) {
        log.info("Updating project {} for user: {}", id, owner.getEmail());
        
        Project project = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Project not found for update: {} for user: {}", id, owner.getEmail());
                    return new AuthException("Проект не найден", "id", "Проект с таким ID не найден");
                });
        
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        
        Project updatedProject = projectRepository.saveAndFlush(project);
        log.info("Project updated: {} with status: {}", id, updatedProject.getStatus());
        
        return new ProjectDto(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID id, User owner) {
        log.info("Deleting project {} for user: {}", id, owner.getEmail());
        
        Project project = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> {
                    log.warn("Project not found for delete: {} for user: {}", id, owner.getEmail());
                    return new AuthException("Проект не найден", "id", "Проект с таким ID не найден");
                });
        
        projectRepository.delete(project);
        log.info("Project deleted: {}", id);
    }
}

