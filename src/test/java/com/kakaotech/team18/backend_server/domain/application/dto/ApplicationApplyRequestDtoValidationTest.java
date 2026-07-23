package com.kakaotech.team18.backend_server.domain.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("지원서 제출 요청 검증")
class ApplicationApplyRequestDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void afterAll() {
        factory.close();
    }

    private ApplicationApplyRequestDto requestWithDepartment(String department) {
        return new ApplicationApplyRequestDto(
                "a@b.com", "김지원", "220123", "010-1234-5678", department, Gender.MALE, List.of());
    }

    private Set<ConstraintViolation<ApplicationApplyRequestDto>> validate(
            ApplicationApplyRequestDto dto) {
        return validator.validate(dto);
    }

    private boolean hasViolationOn(Set<ConstraintViolation<ApplicationApplyRequestDto>> violations,
            String field) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @DisplayName("정상적인 학과 입력은 통과한다")
    @ParameterizedTest
    @ValueSource(strings = {"컴퓨터공학과", "컴퓨터 공학과", "Computer Science", "AI융합학부 2전공"})
    void acceptsValidDepartment(String department) {
        assertThat(hasViolationOn(validate(requestWithDepartment(department)), "department")).isFalse();
    }

    @DisplayName("학과가 최대 길이를 초과하면 거절한다")
    @Test
    void rejectsTooLongDepartment() {
        String tooLong = "가".repeat(ApplicationApplyRequestDto.DEPARTMENT_MAX_LENGTH + 1);

        assertThat(hasViolationOn(validate(requestWithDepartment(tooLong)), "department")).isTrue();
    }

    @DisplayName("최대 길이까지는 허용한다")
    @Test
    void acceptsDepartmentAtMaxLength() {
        String atLimit = "가".repeat(ApplicationApplyRequestDto.DEPARTMENT_MAX_LENGTH);

        assertThat(hasViolationOn(validate(requestWithDepartment(atLimit)), "department")).isFalse();
    }

    @DisplayName("허용하지 않는 문자가 포함되면 거절한다")
    @ParameterizedTest
    @ValueSource(strings = {"<script>", "컴퓨터공학과!", "컴퓨터@공학과", "컴퓨터'공학과", "컴퓨터<b>공학과"})
    void rejectsDisallowedCharacters(String department) {
        // 자유 입력값이 공개 통계 API로 그대로 노출되므로 입력 단계에서 막는다.
        assertThat(hasViolationOn(validate(requestWithDepartment(department)), "department")).isTrue();
    }

    @DisplayName("학과는 필수 입력이다")
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankDepartment(String department) {
        assertThat(hasViolationOn(validate(requestWithDepartment(department)), "department")).isTrue();
    }

    @DisplayName("학번은 6자리 숫자만 허용한다")
    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234567", "22012a", "abcdef"})
    void rejectsNonSixDigitStudentId(String studentId) {
        ApplicationApplyRequestDto dto = new ApplicationApplyRequestDto(
                "a@b.com", "김지원", studentId, "010-1234-5678", "컴퓨터공학과", Gender.MALE, List.of());

        assertThat(hasViolationOn(validate(dto), "studentId")).isTrue();
    }

    @DisplayName("성별은 선택 입력이라 비워 두어도 통과한다")
    @Test
    void allowsNullGender() {
        ApplicationApplyRequestDto dto = new ApplicationApplyRequestDto(
                "a@b.com", "김지원", "220123", "010-1234-5678", "컴퓨터공학과", null, List.of());

        assertThat(validate(dto)).isEmpty();
    }
}
