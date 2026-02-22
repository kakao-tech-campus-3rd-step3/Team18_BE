package com.kakaotech.team18.backend_server.domain.user.dto;

import com.kakaotech.team18.backend_server.domain.user.entity.User;

public record MyProfileResponseDto(
        Long userId,
        String name,
        String studentId,
        String department,
        String phoneNumber,
        String email) {
    public static MyProfileResponseDto from(User user) {
        return new MyProfileResponseDto(
                user.getId(),
                user.getName(),
                user.getStudentId(),
                user.getDepartment(),
                user.getPhoneNumber(),
                user.getEmail());
    }
}
