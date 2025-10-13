package com.kakaotech.team18.backend_server.domain.club.dto;

import com.kakaotech.team18.backend_server.domain.club.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "동아리 상세 정보 요청 데이터")
public record ClubDetailRequestDto(
        Long  clubId,

        @NotBlank
        String clubName,

        String location,

        @NotNull
        Category category,

        String shortIntroduction,

        List<String> introductionImages,

        String introductionOverview,

        String introductionActivity,

        String introductionIdeal,

        String regularMeetingInfo,

        String recruitStatus,

        String presidentName,

        String presidentPhoneNumber,

        LocalDateTime recruitStart,

        LocalDateTime recruitEnd,

        String applicationNotices
) {}