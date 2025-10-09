package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "동아리 목록 조회 시 응답되는 기본 정보")
public record ClubListResponseDto(
        List<ClubsInfo> clubs
) {
    public record ClubsInfo(
            @Schema(description = "동아리 고유 ID", example = "1") Long id,
            @Schema(description = "동아리 이름", example = "개발하는 사람들") String name,
            @Schema(description = "동아리 카테고리", example = "STUDY") Category category,
            @Schema(description = "동아리 한 줄 소개", example = "함께 성장하는 개발 동아리입니다.") String shortIntroduction,
            @Schema(description = "모집 상태", example = "모집중") String recruitStatus
    ){}

    public static ClubsInfo from(Club club) {
        return new ClubsInfo(
                club.getId(),
                club.getName(),
                club.getCategory(),
                club.getShortIntroduction(),
                RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd()).getDisplayName()
        );
    }

    public static ClubsInfo from(ClubSummary summary, String recruitStatus) {
        return new ClubsInfo(
                summary.getId(),
                summary.getName(),
                summary.getCategory(),
                summary.getShortIntroduction(),
                recruitStatus
        );
    }
}
