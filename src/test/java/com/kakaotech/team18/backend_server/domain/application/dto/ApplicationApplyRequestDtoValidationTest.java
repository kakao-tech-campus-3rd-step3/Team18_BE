package com.kakaotech.team18.backend_server.domain.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.kakaotech.team18.backend_server.domain.user.entity.Faculty;
import com.kakaotech.team18.backend_server.domain.user.entity.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApplicationApplyRequestDto 검증 - 학부(faculty, 선택 입력)")
class ApplicationApplyRequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ApplicationApplyRequestDto dtoWithFaculty(Faculty faculty) {
        return new ApplicationApplyRequestDto(
                "stud@example.com", "홍길동", "202312", "010-0000-0000",
                "컴퓨터공학과", Gender.MALE, faculty, List.of()
        );
    }

    @Test
    @DisplayName("faculty가 null이어도 검증 위반 없음 (프론트 전환기 미전송 허용)")
    void faculty_null_isAllowed() {
        Set<ConstraintViolation<ApplicationApplyRequestDto>> violations =
                validator.validate(dtoWithFaculty(null));

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("faculty"));
    }

    @Test
    @DisplayName("유효한 faculty 값이면 faculty 관련 위반 없음")
    void faculty_valid_noViolation() {
        Set<ConstraintViolation<ApplicationApplyRequestDto>> violations =
                validator.validate(dtoWithFaculty(Faculty.ENGINEERING));

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("faculty"));
    }

    @Test
    @DisplayName("목록에 없는 학부는 ETC로 접수 가능")
    void faculty_etc_isAccepted() {
        Set<ConstraintViolation<ApplicationApplyRequestDto>> violations =
                validator.validate(dtoWithFaculty(Faculty.ETC));

        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals("faculty"));
    }
}
