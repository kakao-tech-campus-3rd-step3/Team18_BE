package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "지원서 양식 조회 응답 데이터")
public record ClubApplyFormResponseDto(
        @Schema(description = "지원서 양식 제목", example = "2024년 2학기 신입 부원 모집")
        String title,
        @Schema(description = "지원서 양식 설명", example = "함께 성장할 열정적인 신입 부원을 모집합니다!")
        String description,
        @Schema(description = "모집 기간", example = "2024-09-01 ~ 2024-09-30")
        String recruitDate,
        @Schema(description = "질문 목록")
        List<FormQuestionResponseDto> formQuestions
) {
    public static ClubApplyFormResponseDto of(
            String title,
            String description,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            List<FormQuestionResponseDto> questions
    ) {
        String recruitDate = (recruitStart != null && recruitEnd != null)
                ? String.format("%s ~ %s", recruitStart.toLocalDate(), recruitEnd.toLocalDate())
                : null;
        return new ClubApplyFormResponseDto(title, description, recruitDate, questions);
    }
}
