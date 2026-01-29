package com.kakaotech.team18.backend_server.domain.clubMember.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "club_member_profile")
public class ClubMemberProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_member_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_member_id", nullable = false, unique = true)
    private ClubMember clubMember;

    @Column(nullable = false)
    private String name;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String college;

    @Column(nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    private ClubMemberProfile(ClubMember clubMember, String name, String studentId,
                              String phoneNumber, String college, String department,
                              AcademicStatus academicStatus, LocalDate joinDate, Role role) {
        this.clubMember = clubMember;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.college = college;
        this.department = department;
        this.academicStatus = academicStatus;
        this.joinDate = joinDate;
        this.role = role;
    }

    public void update(String name, String studentId, String phoneNumber, String college,
                       String department, AcademicStatus academicStatus, LocalDate joinDate) {
        this.name = getOrDefault(name, this.name);
        this.studentId = getOrDefault(studentId, this.studentId);
        this.phoneNumber = getOrDefault(phoneNumber, this.phoneNumber);
        this.college = getOrDefault(college, this.college);
        this.department = getOrDefault(department, this.department);
        this.academicStatus = getOrDefault(academicStatus, this.academicStatus);
        this.joinDate = getOrDefault(joinDate, this.joinDate);
    }

    public void updateRole(Role role) {
        if (role != null) this.role = role;
    }

    public boolean isOwner(Long userId) {
        return this.clubMember.getUser().getId().equals(userId);
    }

    private String getOrDefault(String newValue, String oldValue) {
        if (newValue != null && !newValue.isBlank()) {
            return newValue;
        }
        return oldValue;
    }

    private <T> T getOrDefault(T newValue, T oldValue) {
        if (newValue != null) {
            return newValue;
        }
        return oldValue;
    }
}
