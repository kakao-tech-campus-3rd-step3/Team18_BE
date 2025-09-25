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

    @Column(unique = true, nullable = true)
    private Long kakaoId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "department",  nullable = false)
    private String department;

    @Builder
    private User(Long kakaoId, String email, String name, String studentId,
            String phoneNumber, String department) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }

    /**
     * 지원서 제출 등으로 먼저 생성된 User 객체에, 나중에 카카오 로그인을 통해 얻은 kakaoId를 연결합니다.
     * @param kakaoId 카카오로부터 발급받은 사용자의 고유 ID
     */
    public void connectKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }
}
