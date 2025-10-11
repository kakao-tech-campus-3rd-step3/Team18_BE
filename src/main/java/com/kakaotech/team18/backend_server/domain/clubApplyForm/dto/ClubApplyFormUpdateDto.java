package com.kakaotech.team18.backend_server.domain.clubApplyForm.dto;

import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionUpdateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "지원서 양식 수정 정보")
public record ClubApplyFormUpdateDto(
        @Schema(description = "지원서 제목", example = "카태켐 12기 지원서")
        @NotBlank(message = "제목은 필수 값입니다.")
        String title,

        @Schema(description = "지원서 설명", example = "카카오테크 캠퍼스 12기 모집을 위한 지원서입니다.")
        @NotBlank(message = "설명은 필수 값입니다.")
        String description,

        @Schema(description = "질문 목록")
        @NotEmpty(message = "질문 목록은 최소 1개 이상이어야 합니다.")
        @Valid
        List<@Valid FormQuestionUpdateDto> formQuestions
) {

}
