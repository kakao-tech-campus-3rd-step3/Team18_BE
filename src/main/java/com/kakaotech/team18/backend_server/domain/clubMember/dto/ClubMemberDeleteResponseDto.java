package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동아리원 삭제 응답 DTO")
public record ClubMemberDeleteResponseDto(
        @Schema(description = "결과 메시지", example = "해당 동아리원이 목록에서 삭제되었습니다.")
        String message,

        @Schema(description = "삭제된 프로필 ID", example = "501")
        Long deletedClubMemberProfileId
) {
}
