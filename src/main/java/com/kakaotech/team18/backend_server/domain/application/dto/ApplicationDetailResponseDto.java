package com.kakaotech.team18.backend_server.domain.application.dto;

import java.util.List;

// 지원자가 제출한 지원서의 상세 내용과 관련된 DTO
public record ApplicationDetailResponseDto(
    Long applicationId,
    String status,
    ApplicantInfo applicantInfo,
    List<QuestionAndAnswer> questionsAndAnswers
) {
    public record ApplicantInfo(
        Long applicantId,
        String name,
        String department,
        String studentId,
        String email,
        String phoneNumber
    ) {
    }

    public record QuestionAndAnswer(
        String question,
        String answer
    ) {
    }
}
