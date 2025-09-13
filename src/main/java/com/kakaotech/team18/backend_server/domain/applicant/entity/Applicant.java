package com.kakaotech.team18.backend_server.domain.applicant.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import com.kakaotech.team18.backend_server.domain.application.entity.Application;
import com.kakaotech.team18.backend_server.domain.club.entity.Club;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Applicant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "department",  nullable = false)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Builder
    protected Applicant(
            String email,
            String name,
            String studentId,
            String phoneNumber,
            String department,
            Club club,
            Application application) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.club = club;
        this.application = application;
    }
}
