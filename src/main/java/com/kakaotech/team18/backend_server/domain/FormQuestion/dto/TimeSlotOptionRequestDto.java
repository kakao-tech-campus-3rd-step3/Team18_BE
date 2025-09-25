package com.kakaotech.team18.backend_server.domain.FormQuestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "동아리의 면접 가능 날짜")
public record TimeSlotOptionRequestDto(
        @Schema(description = "면접 날짜", example = "2025-09-24")
        @NotBlank
        String date,

        @Schema(description = "가능한 면접 시간")
        @Valid
        TimeRange availableTime
) {
    @Schema(description = "동아리의 면접 가능 시간")
    public record TimeRange(
            @Schema(description = "면접 시작 시간", example = "10:00")
            @NotBlank(message = "면접 시작 시간은 필수 입니다.")
            String start,

            @Schema(description = "면점 마감 시간", example = "21:00")
            @NotBlank(message = "면접 마감 시간은 필수 입니다.")
            String end
    ) {}
}


