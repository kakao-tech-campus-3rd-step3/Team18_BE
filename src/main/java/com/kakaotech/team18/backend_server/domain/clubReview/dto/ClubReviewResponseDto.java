package com.kakaotech.team18.backend_server.domain.clubReview.dto;

import static com.kakaotech.team18.backend_server.domain.clubReview.dto.ClubReviewResponseDto.AnonymousNameGenerator.generate;

import com.kakaotech.team18.backend_server.domain.clubReview.entity.ClubReview;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

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
                        generate(r.getWriter()),
                        r.getContent(),
                        r.getCreatedAt()
                ))
                .toList();

        return new ClubReviewResponseDto(reviews);
    }

    static class AnonymousNameGenerator {

        private static final String[] LOCATIONS = {"호주", "북극", "사하라", "달빛", "숲속", "해변"};
        private static final String[] ADJECTIVES = {"파란", "졸린", "용감한", "사나운", "귀여운"};
        private static final String[] NOUNS = {"여우", "코끼리", "펭귄", "돌고래", "사자", "호랑이"};

        public static String generate(String studentId) {
            int seed = Math.abs(studentId.hashCode()); // 학번 기반 고정 시드
            Random random = new Random(seed);

            String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
            String location = LOCATIONS[random.nextInt(LOCATIONS.length)];
            String noun = NOUNS[random.nextInt(NOUNS.length)];

            return adjective + " " + location + " " + noun;
        }
    }
}
