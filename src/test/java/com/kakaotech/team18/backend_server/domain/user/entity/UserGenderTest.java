package com.kakaotech.team18.backend_server.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User 성별 필드")
class UserGenderTest {

    private User newUser(Gender gender) {
        return User.builder()
                .studentId("220123")
                .email("a@b.com")
                .name("김지원")
                .phoneNumber("010-1234-5678")
                .department("컴퓨터공학과")
                .gender(gender)
                .build();
    }

    @DisplayName("성별은 Enum 이름으로 저장한다")
    @Test
    void persistsGenderAsString() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("gender");
        Enumerated enumerated = field.getAnnotation(Enumerated.class);

        assertThat(enumerated).isNotNull();
        // 순서값(ORDINAL)으로 저장하면 상수를 추가하거나 순서를 바꾸는 순간
        // 이미 저장된 행의 의미가 통째로 달라진다.
        assertThat(enumerated.value()).isEqualTo(EnumType.STRING);
    }

    @DisplayName("성별 컬럼은 nullable이라 기존 데이터가 깨지지 않는다")
    @Test
    void genderColumnIsNullable() throws NoSuchFieldException {
        Column column = User.class.getDeclaredField("gender").getAnnotation(Column.class);

        assertThat(column.nullable()).isTrue();
    }

    @DisplayName("상수를 추가해도 기존 상수의 이름은 그대로 유지된다")
    @Test
    void existingConstantNamesAreStable() {
        // 이름으로 저장하므로, Enum에 상수가 추가되어도 저장된 'MALE'/'FEMALE'의 의미는 변하지 않는다.
        assertThat(Gender.MALE.name()).isEqualTo("MALE");
        assertThat(Gender.FEMALE.name()).isEqualTo("FEMALE");
        assertThat(Gender.valueOf("MALE")).isEqualTo(Gender.MALE);
    }

    @DisplayName("성별이 비어 있으면 이번 제출값으로 채운다")
    @Test
    void fillsGenderWhenAbsent() {
        User user = newUser(null);

        assertThat(user.fillGenderIfAbsent(Gender.FEMALE)).isTrue();
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
    }

    @DisplayName("이미 성별이 있으면 덮어쓰지 않는다")
    @Test
    void doesNotOverwriteExistingGender() {
        User user = newUser(Gender.MALE);

        assertThat(user.fillGenderIfAbsent(Gender.FEMALE)).isFalse();
        assertThat(user.getGender()).isEqualTo(Gender.MALE);
    }

    @DisplayName("제출값이 없으면 기존 null을 그대로 둔다")
    @Test
    void keepsNullWhenNothingSubmitted() {
        User user = newUser(null);

        assertThat(user.fillGenderIfAbsent(null)).isFalse();
        assertThat(user.getGender()).isNull();
    }
}
