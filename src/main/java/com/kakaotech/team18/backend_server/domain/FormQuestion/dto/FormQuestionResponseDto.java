package com.kakaotech.team18.backend_server.domain.FormQuestion.dto;

import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FieldType;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.FormQuestion;
import com.kakaotech.team18.backend_server.domain.FormQuestion.entity.TimeSlotOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지원서 양식 내 개별 질문 정보")
public record FormQuestionResponseDto(
        @Schema(description = "질문 ID", example = "1")
        Long questionId,

        @Schema(description = "질문 번호 (표시 순서)", example = "1")
        Long questionNum,

        @Schema(description = "질문 유형", example = "CHECK_BOX")
        FieldType questionType,

        @Schema(description = "질문 내용", example = "가장 자신 있는 프로그래밍 언어는 무엇인가요?")
        String question,

        @Schema(description = "필수 응답 여부", example = "true")
        boolean required,

        @Schema(description = "선택지 목록 (객관식, 체크박스 유형일 경우에만 존재)", example = "[\"JAVA\", \"C\", \"C++\"]")
        List<String> optionList,

        @Schema(description = "(Time Slot)선택지")
        List<TimeSlotOption> timeSlotOptions

) {

    public static FormQuestionResponseDto from(FormQuestion field) {
        return new FormQuestionResponseDto(
                field.getId(),
                field.getDisplayOrder(),
                field.getFieldType(),
                field.getQuestion(),
                field.isRequired(),
                field.getOptions(),
                field.getTimeSlotOptions()
        );
    }
}
