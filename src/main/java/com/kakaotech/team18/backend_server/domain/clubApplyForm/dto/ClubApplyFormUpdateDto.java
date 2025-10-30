package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionUpdateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Size;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Schema(description = "지원서 양식 수정 정보")
public record ClubApplyFormUpdateDto(
        @Schema(description = "지원서 제목", example = "카태켐 12기 지원서")
        @NotBlank(message = "제목은 필수 값입니다.")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요.")
        String title,

        @Schema(description = "지원서 설명", example = "카카오테크 캠퍼스 12기 모집을 위한 지원서입니다.")
        @NotBlank(message = "설명은 필수 값입니다.")
        @Size(max = 100, message = "설명은 100자 이내로 입력해주세요.")
        String description,

        @Schema(description = "모집 일정", example = "2025-03-01 ~ 2025-03-31")
        @NotNull(message = "모집 일정은 필수입니다.")
        String recruitDate,

        @Schema(description = "질문 목록")
        @NotEmpty(message = "질문 목록은 최소 1개 이상이어야 합니다.")
        @Valid
        List<@Valid FormQuestionUpdateDto> formQuestions
) {
        @AssertTrue(message = "모집 마감일은 시작일과 같거나 이후여야 합니다.")
        @JsonIgnore
        @Schema(hidden = true)
        public boolean isRecruitPeriodValid() {
                try {
                        // 공백 제거 후 "~" 기준으로 split
                        String[] recruitDates = recruitDate.replaceAll("\\s+", "").split("~");

                        // 날짜 형식 지정 (yyyy-MM-dd)
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        // 문자열 → LocalDate 변환
                        LocalDate startDate = LocalDate.parse(recruitDates[0], formatter);
                        LocalDate endDate = LocalDate.parse(recruitDates[1], formatter);

                        // LocalDateTime 변환 (하루 시작/끝 기준)
                        LocalDateTime recruitStart = startDate.atStartOfDay();
                        LocalDateTime recruitEnd = endDate.atTime(23, 59, 59);

                        // 검증 로직: 마감일이 시작일보다 같거나 이후여야 함
                        return !recruitEnd.isBefore(recruitStart);
                } catch (Exception e) {
                        // 파싱 실패 시 false 반환 (유효하지 않은 입력)
                        log.warn("Invalid recruitDate format: '{}'", recruitDate, e);
                        return false;
                }
        }
}
