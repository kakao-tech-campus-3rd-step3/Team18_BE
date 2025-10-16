package com.kakaotech.team18.backend_server.domain.email.dto;

import java.time.LocalDateTime;

public record ApplicationInfoDto(
        String clubName,
        String userName,
        Long clubId,
        String presidentEmail,
        String studentId,
        String userDepartment,
        String userPhoneNumber,
        String userEmail,
        LocalDateTime LastModifiedAt
        ) {
}
