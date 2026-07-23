package com.kakaotech.team18.backend_server.domain.application.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("학과 입력 정규화")
class DepartmentNormalizerTest {

    @DisplayName("앞뒤 공백뿐 아니라 문자열 중간의 공백까지 제거한다")
    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "  컴퓨터공학과  | 컴퓨터공학과",
            "컴퓨터 공학과   | 컴퓨터공학과",
            "  컴퓨터 공학 과 | 컴퓨터공학과",
            "컴퓨터공학과     | 컴퓨터공학과",
            "Computer Science | ComputerScience"
    })
    void removesAllWhitespace(String raw, String expected) {
        assertThat(DepartmentNormalizer.normalize(raw)).isEqualTo(expected);
    }

    @DisplayName("탭과 개행도 공백으로 보고 제거한다")
    @Test
    void removesTabAndNewline() {
        assertThat(DepartmentNormalizer.normalize("컴퓨터\t공학\n과")).isEqualTo("컴퓨터공학과");
    }

    @DisplayName("같은 학과를 다르게 띄어 쓴 입력은 같은 값으로 모인다")
    @Test
    void differentSpacingCollapsesToSameBucket() {
        String a = DepartmentNormalizer.normalize("컴퓨터 공학과");
        String b = DepartmentNormalizer.normalize(" 컴퓨터공학과 ");

        assertThat(a).isEqualTo(b);
    }

    @DisplayName("빈 입력과 공백뿐인 입력은 '미입력'으로 저장한다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void blankBecomesUnknown(String raw) {
        assertThat(DepartmentNormalizer.normalize(raw))
                .isEqualTo(DepartmentNormalizer.UNKNOWN_DEPARTMENT);
    }

    @DisplayName("표기 자체가 다르면 정규화로 합쳐지지 않는다 (감수하는 파편화)")
    @Test
    void doesNotUnifyDifferentNotations() {
        assertThat(DepartmentNormalizer.normalize("컴퓨터공학부"))
                .isNotEqualTo(DepartmentNormalizer.normalize("컴퓨터공학과"));
        assertThat(DepartmentNormalizer.normalize("컴공"))
                .isNotEqualTo(DepartmentNormalizer.normalize("컴퓨터공학과"));
    }
}
