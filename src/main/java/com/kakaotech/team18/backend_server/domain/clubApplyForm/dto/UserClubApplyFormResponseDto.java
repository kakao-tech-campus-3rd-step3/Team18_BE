package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.kakaotech.team18.backend_server.domain.formQuestion.dto.UserFormQuestionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "지원서 양식 조회 응답 데이터")
public record UserClubApplyFormResponseDto(
        @Schema(description = "지원서 양식 제목", example = "2024년 2학기 신입 부원 모집")
        String title,
        @Schema(description = "지원서 양식 설명", example = "함께 성장할 열정적인 신입 부원을 모집합니다!")
        String description,
        @Schema(description = "질문 목록")
        List<UserFormQuestionResponseDto> formQuestions
) {
    public static UserClubApplyFormResponseDto of(
            String title,
            String description,
            List<UserFormQuestionResponseDto> questions
    ) {
        return new UserClubApplyFormResponseDto(title, description, questions);
    }
}