package com.kakaotech.team18.backend_server.domain.user.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String name;

    private String studentId;

    private String phoneNumber;

    private String department;

    @Builder
    private Users(String email, String name, String studentId, String phoneNumber,
            String department) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }
}
