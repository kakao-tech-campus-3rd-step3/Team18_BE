package com.kakaotech.team18.backend_server.domain.applicationFormField.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.applicationForm.entity.ApplicationForm;
import com.kakaotech.team18.backend_server.global.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationFormField extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_form_field_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_form_id")
    private ApplicationForm applicationForm;

    private String question;

    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    private boolean isRequired;

    @Column(name = "display_order")
    private Long displayOrder;

    @Convert(converter = StringListConverter.class)
    private List<String> options;
}
