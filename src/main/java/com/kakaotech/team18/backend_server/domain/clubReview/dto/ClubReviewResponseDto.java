package com.kakaotech.team18.backend_server.domain.clubReview.dto;

import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "동아리 후기 조회 응답 데이터")
public record ClubReviewResponseDto(
        @Schema(description = "후기 목록")
        List<Review> reviews
) {
    public record Review(
            @Schema(description = "후기 id", example = "1")
            Long id,
            @Schema(description = "익명 작성자", example = "익명1")
            String writer,
            @Schema(description = "후기 내용", example = "최고의 개발 동아리입니다.")
            String content,
            @Schema(description = "작성일", example = "2024-07-23T10:00:00")
            LocalDateTime createdAt
    ) {}

    public static ClubReviewResponseDto from(List<ClubReview> clubReviews) {
        List<Review> reviews = clubReviews.stream()
                .map(r -> new Review(
                        r.getId(),
                        getAnonymousName(r.getWriter()),
                        r.getContent(),
                        r.getCreatedAt()
                ))
                .toList();

        return new ClubReviewResponseDto(reviews);
    }

    private static String getAnonymousName(String studentId) {
        int hash = Math.abs(studentId.hashCode());
        int anonNumber = (hash % 1000) + 1;
        return "익명" + anonNumber;
    }
}
