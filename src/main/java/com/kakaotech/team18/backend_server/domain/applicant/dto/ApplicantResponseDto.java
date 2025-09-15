package com.kakaotech.team18.backend_server.domain.applicant.dto;

import com.kakaotech.team18.backend_server.domain.applicant.entity.Applicant;
import com.kakaotech.team18.backend_server.domain.application.entity.Status;

/**
 * 이름, 학번, 학과, 전화번호, 이메일, 결과
 */
public record ApplicantResponseDto(
        String name,
        String studentId,
        String department,
        String phoneNumber,
        String email,
        Status status
) {

    public static ApplicantResponseDto from(Applicant applicant) {
        return new ApplicantResponseDto(
                applicant.getName(),
                applicant.getStudentId(),
                applicant.getDepartment(),
                applicant.getPhoneNumber(),
                applicant.getEmail(),
                applicant.getApplication().getStatus()
        );
    }
}
