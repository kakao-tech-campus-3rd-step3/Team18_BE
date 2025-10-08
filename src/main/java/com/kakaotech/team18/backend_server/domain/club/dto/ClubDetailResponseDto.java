package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubCaution;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubImage;
import com.kakaotech.team18.backend_server.domain.club.entity.ClubIntroduction;
import com.kakaotech.team18.backend_server.domain.club.util.RecruitStatusCalculator;
import com.kakaotech.team18.backend_server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
@Schema(description = "동아리 상세 정보 응답 데이터")
public record ClubDetailResponseDto(
        @Schema(description = "동아리 이름", example = "개발하는 사람들") String clubName,
        @Schema(description = "주요 활동 장소", example = "학생회관 101호") String location,
        @Schema(description = "동아리 카테고리", example = "STUDY") Category category,
        @Schema(description = "동아리 한 줄 소개", example = "함께 성장하는 개발 동아리입니다.") String shortIntroduction,
        @Schema(description = "동아리 소개 이미지 URL 목록") List<String> introductionImages,
        @Schema(description = "동아리 소개 (개요)", example = "저희는 스프링부트와 리액트를 공부하는 스터디 동아리입니다...") String introductionOverview,
        @Schema(description = "동아리 소개 (활동 내용)", example = "매주 월요일 정기 스터디, 분기별 해커톤 진행") String introductionActivity,
        @Schema(description = "동아리 소개 (인재상)", example = "개발에 대한 열정이 넘치는 분") String introductionIdeal,
        @Schema(description = "정기 모임 정보", example = "매주 월요일 18시") String regularMeetingInfo,
        @Schema(description = "모집 상태", example = "모집중") String recruitStatus,
        @Schema(description = "동아리 회장 이름", example = "김회장") String presidentName,
        @Schema(description = "동아리 회장 연락처", example = "010-1234-5678") String presidentPhoneNumber,
        @Schema(description = "모집 시작일") LocalDateTime recruitStart,
        @Schema(description = "모집 마감일") LocalDateTime recruitEnd,
        @Schema(description = "동아리 지원 유의사항 목록") List<ClubCautionResponseDto> applicationNotices
) {

    public static ClubDetailResponseDto from(Club club, User user) {
        Optional<ClubIntroduction> clubIntroduction = Optional.ofNullable(club.getIntroduction());

        return ClubDetailResponseDto.builder().
                clubName(club.getName()).
                location(club.getLocation()).
                category(club.getCategory()).
                shortIntroduction(club.getShortIntroduction()).
                introductionImages(clubIntroduction.map(ClubIntroduction::getImages)
                        .map(images -> images.stream()
                                .map(ClubImage::getImageUrl)
                                .collect(Collectors.toList()))
                        .orElse(List.of())).
                introductionOverview(clubIntroduction.map(ClubIntroduction::getOverview).orElse("")).
                introductionActivity(clubIntroduction.map(ClubIntroduction::getActivities).orElse("")).
                introductionIdeal(clubIntroduction.map(ClubIntroduction::getIdeal).orElse("")).
                regularMeetingInfo(club.getRegularMeetingInfo()).
                recruitStatus(RecruitStatusCalculator.calculate(club.getRecruitStart(), club.getRecruitEnd()).getDisplayName()).
                presidentName(user.getName()).
                presidentPhoneNumber(user.getPhoneNumber()).
                recruitStart(club.getRecruitStart()).
                recruitEnd(club.getRecruitEnd()).
                applicationNotices(club.getCautions().stream()
                        .sorted(Comparator.comparing(ClubCaution::getDisplayOrder))
                        .map(ClubCautionResponseDto::from).toList()).
                build();
    }
}
