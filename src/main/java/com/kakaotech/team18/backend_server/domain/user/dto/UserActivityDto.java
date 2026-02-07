package com.kakaotech.team18.backend_server.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserActivityDto(
        List<UserClubReviewDto> reviews,
        List<UserCommentDto> comments,
        List<UserApplicationDto> applications) {

    public record UserClubReviewDto(
            Long reviewId,
            Long clubId,
            String clubName,
            String content,
            LocalDateTime createdAt) {
    }

    public record UserCommentDto(
            Long commentId,
            Long applicationId,
            Long clubId,
            String clubName,
            String content,
            Double rating,
            LocalDateTime createdAt) {
    }

    public record UserApplicationDto(
            Long applicationId,
            Long clubId,
            String clubName,
            String status,
            LocalDateTime createdAt) {
    }
}
