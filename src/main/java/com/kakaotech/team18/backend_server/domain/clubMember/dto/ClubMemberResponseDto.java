package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMemberProfile;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;

@Schema(description = "동아리원 목록 조회 응답 DTO")
public record ClubMemberResponseDto(
        @Schema(description = "동아리원 프로필 ID", example = "101")
        Long clubMemberProfileId,

        @Schema(description = "이름", example = "이지훈")
        String name,

        @Schema(description = "학과", example = "컴퓨터공학과")
        String department,

        @Schema(description = "학번", example = "213856")
        String studentId,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "동아리 내 직책", example = "CLUB_EXECUTIVE")
        Role role,

        @Schema(description = "가입일자 (YYYY-MM)", example = "2023-03")
        String joinDate
) {
    public static ClubMemberResponseDto from(ClubMemberProfile profile) {
        return new ClubMemberResponseDto(
                profile.getId(),
                profile.getName(),
                profile.getDepartment(),
                profile.getStudentId(),
                profile.getPhoneNumber(),
                profile.getRole(),
                profile.getJoinDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        );
    }
}
