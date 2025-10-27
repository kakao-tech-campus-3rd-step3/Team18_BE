package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "user가 등록된 club의 clubId와 Role 정보를 담은 DTO")
public record ClubIdAndRoleInfoDto(
        @Schema(description = "user가 등록된 club의 clubId")
        Long clubId,
        @Schema(description = "user가 등록된 club의 Role")
        Role role
) {
}
