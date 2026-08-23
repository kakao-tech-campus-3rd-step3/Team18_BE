package com.kakaotech.team18.backend_server.domain.user.entity;

import com.kakaotech.team18.backend_server.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    /**
     * 성별. 선택 입력이며, 이 필드가 추가되기 전에 생성된 User는 null이다.
     * 통계에서는 null을 '미입력' 버킷으로 집계한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    /**
     * 학부(단과대학). 이 필드가 추가되기 전에 생성된 User나 비지원 경로(카카오·동아리원)로 생성된 User는 null이다.
     * 통계에서는 null을 '미입력' 버킷으로 집계한다. 목록에 없는 학부는 {@link Faculty#ETC}로 수집된다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "faculty")
    private Faculty faculty;

    @Builder
    private User(
            Long kakaoId,
            String email,
            String name,
            String studentId,
            String phoneNumber,
            String department,
            Gender gender,
            Faculty faculty
    ) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.gender = gender;
        this.faculty = faculty;
    }

    /**
     * 지원서 제출 등으로 먼저 생성된 User 객체에, 나중에 카카오 로그인을 통해 얻은 kakaoId를 연결합니다.
     * @param kakaoId 카카오로부터 발급받은 사용자의 고유 ID
     */
    public void connectKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }

    /**
     * 성별이 아직 없을 때만 채워 넣습니다.
     * <p>
     * 지원서 제출 시 User는 학번으로 조회되어 재사용되므로(재지원·타 동아리 지원), 이 처리가 없으면 성별 필드 추가
     * 이전에 만들어진 User의 성별은 영원히 null로 남습니다. 이미 값이 있으면 덮어쓰지 않아, 뒤늦은 제출이 기존 응답을
     * 바꾸지 못하게 합니다.
     *
     * @param gender 새로 접수된 성별 (null이면 아무 것도 하지 않음)
     * @return 실제로 값이 채워졌으면 true
     */
    public boolean fillGenderIfAbsent(Gender gender) {
        if (this.gender != null || gender == null) {
            return false;
        }
        this.gender = gender;
        return true;
    }

    /**
     * 학부가 아직 없을 때만 채워 넣습니다.
     * <p>
     * 지원서 제출 시 User는 학번으로 조회되어 재사용되므로(재지원·타 동아리 지원), 이 처리가 없으면 학부 필드 추가
     * 이전에 만들어진 User의 학부는 영원히 null로 남습니다. 이미 값이 있으면 덮어쓰지 않아, 뒤늦은 제출이 기존 응답을
     * 바꾸지 못하게 합니다.
     *
     * @param faculty 새로 접수된 학부 (null이면 아무 것도 하지 않음)
     * @return 실제로 값이 채워졌으면 true
     */
    public boolean fillFacultyIfAbsent(Faculty faculty) {
        if (this.faculty != null || faculty == null) {
            return false;
        }
        this.faculty = faculty;
        return true;
    }
}
