package com.kakaotech.team18.backend_server.domain.clubMember.dto;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.ClubMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "대시보드 내 지원자 목록의 개별 지원자 정보")
public record ApplicantResponseDto(
        @Schema(description = "지원자 이름", example = "김지원")
        String name,
        @Schema(description = "학번", example = "212121")
        String studentId,
        @Schema(description = "학과", example = "컴퓨터공학과")
        String department,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,
        @Schema(description = "이메일", example = "test@test.com")
        String email,
        @Schema(description = "지원서 상태", example = "PENDING")
        Status status,
        @Schema(description = "확정된 지원자의 면접 시간", example = "2026-09-02T15:00:00")
        LocalDateTime confirmedTime,
        @Schema(description = "지원서 ID", example = "1")
        Long applicantId,
        @Schema(description = "지원자의 인터뷰 선호 일정 리스트" )
        List<preferInterviewInfo> interviewInfo
) {

    @Schema(description = "지원자의 인터뷰 선호 일정")
    public record preferInterviewInfo(
            @Schema(description = "면접 날짜", example = "2026-01-02")
            LocalDate interviewDate,

            @Schema(description = "면접 가능 시간 리스트")
            List<LocalTime> availableTime
    ) {}


    public static ApplicantResponseDto from(ClubMember clubMember) {

        LocalDateTime confirmedTime = null;

        if (clubMember.getApplication().getInterviewDate() != null &&
                clubMember.getApplication().getInterviewTime() != null) {
            confirmedTime = LocalDateTime.of(
                    clubMember.getApplication().getInterviewDate(),
                    clubMember.getApplication().getInterviewTime()
            );
        }
        return new ApplicantResponseDto(
                clubMember.getUser().getName(),
                clubMember.getUser().getStudentId(),
                clubMember.getUser().getDepartment(),
                clubMember.getUser().getPhoneNumber(),
                clubMember.getUser().getEmail(),
                clubMember.getApplication().getStatus(),
                confirmedTime,
                clubMember.getApplication().getId(),
                clubMember.getApplication().getInterviewPreferences().stream()
                        .map(pref -> new preferInterviewInfo(pref.getDate(), pref.getTime()))
                        .toList()
        );
    }
}
