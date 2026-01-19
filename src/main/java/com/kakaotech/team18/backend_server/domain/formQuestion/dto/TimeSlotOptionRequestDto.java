package com.kakaotech.team18.backend_server.domain.formQuestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Schema(description = "동아리의 면접 가능 날짜")
public record TimeSlotOptionRequestDto(
        @Schema(description = "면접 날짜", example = "2025-09-24 ~ 2025-09-25")
        @NotNull(message = "면접 날짜는 필수 입니다.")
        String date,

        @Schema(description = "가능한 면접 시간")
        @Valid
        @NotNull(message = "면접 가능 시간은 필수 입니다.")
        TimeRange availableTime
) {
    @AssertTrue(message = "면접 시작 시간은 마감 시간보다 이전이어야 합니다.")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isValid() {
        if (this.availableTime == null || this.availableTime.start() == null || this.availableTime.end() == null) {
            return true; // @NotNull보다 이 검증이 먼저 실행돼서 null 체크를 해줌
        }
        return this.availableTime.start().isBefore(this.availableTime.end());
    }

    @Schema(description = "동아리의 면접 가능 시간")
    public record TimeRange(
            @Schema(description = "면접 시작 시간", example = "10:00")
            @NotNull(message = "면접 시작 시간은 필수 입니다.")
            LocalTime start,

            @Schema(description = "면접 마감 시간", example = "21:00")
            @NotNull(message = "면접 마감 시간은 필수 입니다.")
            LocalTime end
    ) {}
}
