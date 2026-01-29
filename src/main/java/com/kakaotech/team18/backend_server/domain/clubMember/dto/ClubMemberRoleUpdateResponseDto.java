package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리원 직책 변경 응답 DTO")
public record ClubMemberRoleUpdateResponseDto(
        @Schema(description = "동아리 ID", example = "1")
        Long clubId,

        @Schema(description = "동아리 이름", example = "동아리움")
        String clubName,

        @Schema(description = "프로필 ID", example = "501")
        Long clubMemberProfileId,

        @Schema(description = "학번", example = "212121")
        String studentId,

        @Schema(description = "이름", example = "박개명")
        String name,

        @Schema(description = "변경 전 직책", example = "CLUB_MEMBER")
        Role previousRole,

        @Schema(description = "변경 후 직책", example = "CLUB_EXECUTIVE")
        Role newRole,

        @Schema(description = "결과 메시지", example = "운영진으로 역할이 변경되었습니다.")
        String message
) {
}
