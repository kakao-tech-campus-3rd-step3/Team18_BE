package com.kakaotech.team18.backend_server.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 및 수정을 위한 요청 데이터")
public record CommentRequestDto(
        @Schema(description = "댓글 내용 (500자 이하)", requiredMode = Schema.RequiredMode.REQUIRED, example = "지원자의 경험이 우리 동아리와 잘 맞는 것 같습니다.")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 500, message = "댓글은 500자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "별점 (0.5 ~ 5.0 사이)", requiredMode = Schema.RequiredMode.REQUIRED, example = "4.5")
        @NotNull(message = "별점은 필수입니다.")
        @DecimalMin(value = "0.5", message = "별점은 0.5 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
        Double rating
) {
}
