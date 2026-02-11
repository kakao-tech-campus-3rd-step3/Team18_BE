package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "동아리원 직책 변경 요청 DTO")
public record ClubMemberRoleUpdateRequestDto(
        @Schema(description = "변경할 직책 (CLUB_MEMBER, CLUB_EXECUTIVE, CLUB_ADMIN)", example = "CLUB_EXECUTIVE")
        @NotNull(message = "직책은 필수입니다.")
        Role role
) {
}
