package com.reviewer.project.projectMember.model.dto;

import java.time.Instant;

import com.reviewer.enums.ProjectMemberRole;
import com.reviewer.project.projectMember.model.Entity.ProjectMemberEntity;

public record ProjectMemberResponse(
        Long projectMemberId,
        Long userId,
        String name,
        String loginId,
        ProjectMemberRole role,
        boolean active,
        Instant joinedAt
) {
    public static ProjectMemberResponse from(ProjectMemberEntity member) {
        return new ProjectMemberResponse(
                member.getProjectMemberId(),
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getLoginId(),
                member.getProjectMemberRole(),
                member.getUser().isActive(),
                member.getJoinedAt()
        );
    }
}
