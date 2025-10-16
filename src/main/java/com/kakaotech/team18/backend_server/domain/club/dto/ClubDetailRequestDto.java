package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "동아리 상세 정보 등록/수정 요청 데이터")
public record ClubDetailRequestDto(
        @Schema(description = "등록/수정할 동아리 ID", example = "1")
        @NotNull(message = "동아리 ID는 필수입니다.")
        Long  clubId,

        @Schema(description = "등록/수정할 동아리 이름", example = "개발하는 사람들")
        @NotBlank(message = "동아리 이름은 필수입니다.")
        String clubName,

        @Schema(description = "등록/수정할 주요 활동 장소", example = "학생회관 101호")
        @NotBlank(message = "활동 장소는 필수입니다.")
        String location,

        @Schema(description = "등록/수정할 동아리 카테고리", example = "STUDY")
        @NotNull(message = "카테고리는 필수입니다.")
        Category category,

        @Schema(description = "등록/수정할 동아리 한 줄 소개", example = "함께 성장하는 개발 동아리입니다.")
        @NotBlank(message = "한 줄 소개는 필수입니다.")
        String shortIntroduction,

        @Schema(description = "등록/수정할 동아리 소개 이미지 URL 목록")
        List<String> introductionImages,

        @Schema(description = "등록/수정할 동아리 소개 (개요)", example = "저희는 스프링부트와 리액트를 공부하는 스터디 동아리입니다...")
        @NotBlank(message = "소개 개요는 필수입니다.")
        String introductionOverview,

        @Schema(description = "등록/수정할 동아리 소개 (활동 내용)", example = "매주 월요일 정기 스터디, 분기별 해커톤 진행")
        @NotBlank(message = "소개 활동 내용은 필수입니다.")
        String introductionActivity,

        @Schema(description = "등록/수정할 동아리 소개 (인재상)", example = "개발에 대한 열정이 넘치는 분")
        @NotBlank(message = "소개 인재상은 필수입니다.")
        String introductionIdeal,

        @Schema(description = "등록/수정할 정기 모임 정보", example = "매주 월요일 18시")
        @NotBlank(message = "정기 모임 정보는 필수입니다.")
        String regularMeetingInfo,

        String recruitStatus,

        String presidentName,

        String presidentPhoneNumber,

        @Schema(description = "등록/수정할 동아리 지원 유의사항", example = "지원 시 유의사항을 반드시 확인해주세요.")
        @NotBlank(message = "지원 유의사항은 필수입니다.")
        String applicationNotice
) {}