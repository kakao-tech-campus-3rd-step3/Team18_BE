package com.kakaotech.team18.backend_server.domain.applicationForm.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_form_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "title", nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public ApplicationForm(Club club, Application application, String title, String description, boolean isActive) {
        this.club = club;
        this.application = application;
        this.title = title;
        this.description = description;
        this.isActive = isActive;
    }
}
