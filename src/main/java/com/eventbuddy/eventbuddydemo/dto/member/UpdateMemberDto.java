package com.eventbuddy.eventbuddydemo.dto.member;

import com.eventbuddy.eventbuddydemo.model.ProjectMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberDto {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    private ProjectMember.MemberAccess accesses;

    private String tag;
}
