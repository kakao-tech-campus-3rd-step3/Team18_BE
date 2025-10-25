package com.kakaotech.team18.backend_server.domain.formQuestion.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.formQuestion.dto.FormQuestionUpdateDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.TimeSlotOptionRequestDto;
import com.kakaotech.team18.backend_server.domain.formQuestion.dto.TimeSlotOptionRequestDto.TimeRange;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FormQuestionTest {

    @DisplayName("TEXT 타입 FormQuestionUpdateDto를 통해 필드를 수정할 수 있다.")
    @Test
    void updateFrom_shouldUpdateTextFieldsCorrectly() {
        FormQuestion formQuestion = createFormQuestion("오늘 저녁에 먹은 메뉴는?", FieldType.TEXT, 1L, null, null);
        ReflectionTestUtils.setField(formQuestion, "id", 1L);

        FormQuestionUpdateDto dto = new FormQuestionUpdateDto(
                1L, "오늘 먹기 싫은 메뉴는?", FieldType.TEXT, true, 1L, null, null
        );

        formQuestion.updateFrom(dto);

        assertThat(formQuestion).extracting("question", "fieldType", "isRequired", "displayOrder", "options", "timeSlotOptions")
            .containsExactly("오늘 먹기 싫은 메뉴는?", FieldType.TEXT, true, 1L, null, null);
    }

    @DisplayName("CHECKBOX 타입 FormQuestionUpdateDto를 통해 필드를 수정할 수 있다.")
    @Test
    void updateFrom_shouldUpdateCheckboxFieldsCorrectly() {
        FormQuestion formQuestion = createFormQuestion("먹고싶은 메뉴는?", FieldType.CHECKBOX, 2L, List.of("햄버거", "피자", "치킨"), null);
        ReflectionTestUtils.setField(formQuestion, "id", 2L);

        FormQuestionUpdateDto dto = new FormQuestionUpdateDto(
                2L, "먹기 싫은 메뉴는?", FieldType.CHECKBOX, false, 2L, List.of("피자", "치킨"), null
        );

        formQuestion.updateFrom(dto);

        assertThat(formQuestion).extracting("question", "fieldType", "isRequired", "displayOrder", "options", "timeSlotOptions")
            .containsExactly("먹기 싫은 메뉴는?", FieldType.CHECKBOX, false, 2L, List.of("피자", "치킨"), null);
    }

    @DisplayName("RADIO 타입 FormQuestionUpdateDto를 통해 필드를 수정할 수 있다.")
    @Test
    void updateFrom_shouldUpdateRadioFieldsCorrectly() {
        FormQuestion formQuestion = createFormQuestion("성별은?", FieldType.RADIO, 4L, List.of("남", "여"), null);
        ReflectionTestUtils.setField(formQuestion, "id", 4L);

        FormQuestionUpdateDto dto = new FormQuestionUpdateDto(
                2L, "성별?", FieldType.RADIO, false, 4L, List.of("남", "여", "응답안함"), null
        );

        formQuestion.updateFrom(dto);

        assertThat(formQuestion).extracting("question", "fieldType", "isRequired", "displayOrder", "options", "timeSlotOptions")
                .containsExactly("성별?", FieldType.RADIO, false, 4L, List.of("남", "여", "응답안함"), null);
    }

    @DisplayName("TIME_SLOT 타입 FormQuestionUpdateDto를 통해 필드를 수정할 수 있다.")
    @Test
    void updateFrom_shouldUpdateTimeSlotFieldsCorrectly() {
        TimeSlotOption.TimeRange originalTimeRange = new TimeSlotOption.TimeRange("10:00", "18:00");
        TimeSlotOption originalOption = new TimeSlotOption(LocalDate.of(2025, 9, 25), originalTimeRange);
        FormQuestion formQuestion = createFormQuestion("면접 가능한 시간대는?", FieldType.TIME_SLOT, 3L, null, List.of(originalOption));
        ReflectionTestUtils.setField(formQuestion, "id", 3L);

        TimeSlotOptionRequestDto.TimeRange newTimeRange = new TimeRange("22:00", "23:00");
        TimeSlotOptionRequestDto newOption = new TimeSlotOptionRequestDto("2025-09-26", newTimeRange);

        FormQuestionUpdateDto dto = new FormQuestionUpdateDto(
                3L, "면접 불가능한 시간대는?", FieldType.TIME_SLOT, true, 3L, null, List.of(newOption)
        );

        formQuestion.updateFrom(dto);

        assertThat(formQuestion).extracting("question", "fieldType", "isRequired", "displayOrder", "options")
            .containsExactly("면접 불가능한 시간대는?", FieldType.TIME_SLOT, true, 3L, null);

        assertThat(formQuestion.getTimeSlotOptions()).hasSize(1);
        assertThat(formQuestion.getTimeSlotOptions().get(0).date()).isEqualTo(LocalDate.of(2025, 9, 26));
        assertThat(formQuestion.getTimeSlotOptions().get(0).availableTime().start()).isEqualTo("22:00");
        assertThat(formQuestion.getTimeSlotOptions().get(0).availableTime().end()).isEqualTo("23:00");
    }

    private static FormQuestion createFormQuestion(
            String question,
            FieldType fieldType,
            long displayOrder,
            List<String> options,
            List<TimeSlotOption> timeSlotOptions) {
        return FormQuestion.builder()
                .question(question)
                .fieldType(fieldType)
                .isRequired(true)
                .displayOrder(displayOrder)
                .options(options)
                .timeSlotOptions(timeSlotOptions)
                .build();
    }
}