package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;

public record ApplicantResponseDto(
        String name,
        String studentId,
        String department,
        String phoneNumber,
        String email,
        Status status
) {

    public static ApplicantResponseDto from(ClubMember clubMember) {
        return new ApplicantResponseDto(
                clubMember.getUser().getName(),
                clubMember.getUser().getStudentId(),
                clubMember.getUser().getDepartment(),
                clubMember.getUser().getPhoneNumber(),
                clubMember.getUser().getEmail(),
                clubMember.getApplication().getStatus()
        );
    }
}
