package com.kakaotech.team18.backend_server.domain.FormQuestion.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.FormQuestion.dto.FormQuestionUpdateDto;
import com.kakaotech.team18.backend_server.domain.clubApplyForm.entity.ClubApplyForm;
import com.kakaotech.team18.backend_server.global.converter.StringListConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FormQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_apply_form_id", nullable = false)
    private ClubApplyForm clubApplyForm;

    @Column(name = "question", nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "display_order", nullable = false)
    private Long displayOrder;

    @Convert(converter = StringListConverter.class)
    private List<String> options;

    @ElementCollection
    @CollectionTable(name = "time_slot_options", joinColumns = @JoinColumn(name = "form_question_id"))
    private List<TimeSlotOption> timeSlotOptions;

    @Builder
    private FormQuestion(
            ClubApplyForm clubApplyForm,
            String question,
            FieldType fieldType,
            boolean isRequired,
            Long displayOrder,
            List<String> options,
            List<TimeSlotOption> timeSlotOptions) {
        this.clubApplyForm = clubApplyForm;
        this.question = question;
        this.fieldType = fieldType;
        this.isRequired = isRequired;
        this.displayOrder = displayOrder;
        this.options = options;
        this.timeSlotOptions = timeSlotOptions;
    }

    public void updateFrom(FormQuestionUpdateDto dto) {
        this.question = dto.question();
        this.fieldType = dto.fieldType();
        this.isRequired = dto.isRequired();
        this.displayOrder = dto.displayOrder();

        if (dto.fieldType() == FieldType.TIME_SLOT) {
            this.timeSlotOptions = dto.timeSlotOptions() != null
                    ? dto.timeSlotOptions().stream()
                    .map(tsoDto -> new TimeSlotOption(
                            LocalDate.parse(tsoDto.date()),
                            new TimeSlotOption.TimeRange(
                                    tsoDto.availableTime().start(),
                                    tsoDto.availableTime().end()
                            )
                    ))
                    .toList()
                    : null;

            this.options = null;
        } else {
            this.options = dto.optionList();
            this.timeSlotOptions = null;
        }
    }}
