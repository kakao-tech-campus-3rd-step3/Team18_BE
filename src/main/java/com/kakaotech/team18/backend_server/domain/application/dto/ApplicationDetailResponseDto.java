package com.kakaotech.team18.backend_server.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지원서 상세 조회 응답 데이터")
public record ApplicationDetailResponseDto(
    @Schema(description = "지원서 고유 ID", example = "100")
    Long applicationId,

    @Schema(description = "지원서 상태 (예: PENDING, APPROVED, REJECTED)", example = "PENDING")
    String status,

    @Schema(description = "지원자 정보")
    ApplicantInfo applicantInfo,

    @Schema(description = "질문 및 답변 목록")
    List<QuestionAndAnswer> questionsAndAnswers
) {
    @Schema(description = "지원자 상세 정보")
    public record ApplicantInfo(
        @Schema(description = "지원자 고유 ID (User ID)", example = "12")
        Long applicantId,

        @Schema(description = "이름", example = "김지원")
        String name,

        @Schema(description = "학과", example = "컴퓨터공학과")
        String department,

        @Schema(description = "학번", example = "212121")
        String studentId,

        @Schema(description = "이메일", example = "test@test.com")
        String email,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber
    ) {
    }

    @Schema(description = "질문과 그에 대한 답변")
    public record QuestionAndAnswer(
        @Schema(description = "질문 내용", example = "자기소개를 해주세요.")
        String question,

        @Schema(description = "답변 내용", example = "안녕하세요, 저는 개발에 대한 열정을 가진 김지원입니다.")
        String answer
    ) {
    }
}
