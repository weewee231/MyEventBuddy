package com.eventbuddy.eventbuddydemo.service;

import com.eventbuddy.eventbuddydemo.dto.member.DeleteMemberDto;
import com.eventbuddy.eventbuddydemo.dto.member.InviteMemberDto;
import com.eventbuddy.eventbuddydemo.dto.member.MemberDto;
import com.eventbuddy.eventbuddydemo.dto.member.UpdateMemberDto;
import com.eventbuddy.eventbuddydemo.exception.AuthException;
import com.eventbuddy.eventbuddydemo.model.Project;
import com.eventbuddy.eventbuddydemo.model.ProjectMember;
import com.eventbuddy.eventbuddydemo.model.User;
import com.eventbuddy.eventbuddydemo.repository.ProjectMemberRepository;
import com.eventbuddy.eventbuddydemo.repository.ProjectRepository;
import com.eventbuddy.eventbuddydemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<MemberDto> getMembers(UUID projectId, User currentUser, String search, String sort) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        log.info("Getting members for project: {} search={}, sort={}", projectId, search, sort);

        List<MemberDto> members = memberRepository.findByProjectOrderByJoinedAtAsc(project)
                .stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            members = members.stream()
                    .filter(m -> m.getName().toLowerCase().contains(searchLower)
                            || m.getEmail().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        Comparator<MemberDto> comparator = switch (sort != null ? sort : "name") {
            case "email" -> Comparator.comparing(MemberDto::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "accesses" -> Comparator.comparing(m -> m.getAccesses().name());
            case "tag" -> Comparator.comparing(
                    MemberDto::getTag,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            default -> Comparator.comparing(MemberDto::getName, String.CASE_INSENSITIVE_ORDER);
        };

        members.sort(comparator);
        return members;
    }

    @Transactional
    public MemberDto inviteMember(UUID projectId, InviteMemberDto dto, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        log.info("Inviting member {} to project: {}", dto.getEmail(), projectId);

        User invitedUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AuthException(
                        "Пользователь не найден", "email", "Пользователь с таким email не найден"
                ));

        if (invitedUser.getId().equals(project.getOwner().getId())) {
            throw new AuthException("Ошибка приглашения", "email", "Этот пользователь является владельцем проекта");
        }

        if (memberRepository.existsByProjectAndUser(project, invitedUser)) {
            throw new AuthException("Ошибка приглашения", "email", "Пользователь уже является участником проекта");
        }

        ProjectMember member = new ProjectMember(project, invitedUser, ProjectMember.MemberAccess.MEMBER);
        ProjectMember savedMember = memberRepository.save(member);

        log.info("Member {} invited to project: {}", dto.getEmail(), projectId);
        return new MemberDto(savedMember);
    }

    @Transactional
    public MemberDto updateMember(UUID projectId, UpdateMemberDto dto, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        log.info("Updating member {} in project: {}", dto.getEmail(), projectId);

        User memberUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AuthException(
                        "Пользователь не найден", "email", "Пользователь с таким email не найден"
                ));

        ProjectMember member = memberRepository.findByProjectAndUser(project, memberUser)
                .orElseThrow(() -> new AuthException(
                        "Участник не найден", "email", "Этот пользователь не является участником проекта"
                ));

        if (member.getAccesses() == ProjectMember.MemberAccess.OWNER) {
            throw new AuthException("Ошибка обновления", "accesses", "Нельзя изменить роль владельца");
        }

        if (dto.getAccesses() != null) {
            member.setAccesses(dto.getAccesses());
        }

        if (dto.getTag() != null) {
            member.setTag(dto.getTag());
        }

        ProjectMember updatedMember = memberRepository.save(member);
        log.info("Member {} updated in project: {}", dto.getEmail(), projectId);
        return new MemberDto(updatedMember);
    }

    @Transactional
    public void deleteMember(UUID projectId, DeleteMemberDto dto, User currentUser) {
        Project project = getProjectWithAccessCheck(projectId, currentUser);
        log.info("Deleting member {} from project: {}", dto.getEmail(), projectId);

        User memberUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new AuthException(
                        "Пользователь не найден", "email", "Пользователь с таким email не найден"
                ));

        if (memberUser.getId().equals(project.getOwner().getId())) {
            throw new AuthException("Ошибка удаления", "email", "Нельзя удалить владельца проекта");
        }

        ProjectMember member = memberRepository.findByProjectAndUser(project, memberUser)
                .orElseThrow(() -> new AuthException(
                        "Участник не найден", "email", "Этот пользователь не является участником проекта"
                ));

        memberRepository.delete(member);
        log.info("Member {} deleted from project: {}", dto.getEmail(), projectId);
    }

    private Project getProjectWithAccessCheck(UUID projectId, User currentUser) {
        return projectRepository.findByIdAndOwner(projectId, currentUser)
                .orElseThrow(() -> new AuthException(
                        "Проект не найден", "id", "Проект не найден или у вас нет доступа"
                ));
    }
}
