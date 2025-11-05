package com.kakaotech.team18.backend_server.domain.clubReview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "동아리 후기 등록 요청 데이터")
public record ClubReviewRequestDto(
        @Schema(description = "동아리 후기 내용", example = "최괴의 개발 동아리입니다.")
        @NotBlank(message = "동아리 후기는 필수입니다.")
        String content,

        @Schema(description = "작성자 학번", example = "20221234")
        @NotBlank(message = "작성자 학번은 필수입니다.")
        String studentId
) {

}
