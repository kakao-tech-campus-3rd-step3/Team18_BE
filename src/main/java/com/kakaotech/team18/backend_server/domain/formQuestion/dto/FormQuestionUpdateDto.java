package com.kakaotech.team18.backend_server.domain.formQuestion.dto;

import com.kakaotech.team18.backend_server.domain.formQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.formQuestion.validate.ValidFormQuestionRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@ValidFormQuestionRequest
@Schema(description = "지원서 양식 개별 질문 수정 정보")
public record FormQuestionUpdateDto(
        @Schema(description = "질문 ID", example = "1")
        Long questionNum,

        @Schema(description = "질문 내용", example = "가장 자신 있는 프로그래밍 언어는 무엇인가요?")
        @NotBlank(message = "질문은 필수 입니다.")
        String question,

        @Schema(description = "질문 유형", example = "[TEXT, RADIO, CHECKBOX, TIME_SLOT 중 하나 입력 가능]")
        @NotNull(message = "질문 유형은 필수 입니다.")
        FieldType fieldType,

        @Schema(description = "필수 응답 여부", example = "true")
        @NotNull(message = "필수 응답 여부는 필수 입니다.")
        Boolean isRequired,

        @Schema(description = "질문 표시 순서", example = "1")
        @NotNull(message = "질문 표시 순서는 필수 입니다.")
        @Positive(message = "질문 표시 순서는 1 이상이어야 합니다.")
        Long displayOrder,

        @Schema(description = "(check box, radio)선택지", example = "[\"JAVA\", \"C\", \"C++\"]")
        List<String> optionList,

        @Schema(description = "(Time Slot)선택지")
        List<TimeSlotOptionRequestDto> timeSlotOptions
) implements FormQuestionBaseDto{

}
