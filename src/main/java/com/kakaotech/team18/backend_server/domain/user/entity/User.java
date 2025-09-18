package com.kakaotech.team18.backend_server.domain.user.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.*;
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
    private User(String email, String name, String studentId, String phoneNumber,
                 String department) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }
}
