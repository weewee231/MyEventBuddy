package com.eventbuddy.eventbuddydemo.dto.member;

import com.eventbuddy.eventbuddydemo.model.ProjectMember;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    private String name;
    private String email;
    private ProjectMember.MemberAccess accesses;
    private String tag;

    public MemberDto(ProjectMember member) {
        this.name = member.getUser().getName();
        this.email = member.getUser().getEmail();
        this.accesses = member.getAccesses();
        this.tag = member.getTag();
    }
}
