package com.kakaotech.team18.backend_server.domain.user.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false,  unique = true)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

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

    @Builder
    private User(String loginId, String password, String email, String name, String studentId,
            String phoneNumber, String department) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }
}
