package com.kakaotech.team18.backend_server.domain.statistics.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("학번 -> 입학연도 변환")
class AdmissionYearBucketerTest {

    private static final int BASE_YEAR = 2026;
    private static final int MIN_YEAR = 1990;

    @DisplayName("두 자리 연도를 네 자리로 정규화한다")
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "220123, 2022",
            "260001, 2026",
            "200999, 2020"
    })
    void normalizesTwoDigitYear(String studentId, int expected) {
        assertThat(AdmissionYearBucketer.toAdmissionYear(studentId, BASE_YEAR, MIN_YEAR))
                .isEqualTo(expected);
    }

    @DisplayName("기준 연도의 뒤 두 자리를 넘는 값은 이전 세기로 해석한다")
    @Test
    void interpretsYearsBeyondBaseAsPreviousCentury() {
        // 기준 2026년에 '99'는 2099년일 수 없으므로 1999년으로 본다.
        assertThat(AdmissionYearBucketer.toAdmissionYear("991234", BASE_YEAR, MIN_YEAR))
                .isEqualTo(1999);
        // '27'은 기준 연도를 갓 넘으므로 역시 이전 세기다.
        assertThat(AdmissionYearBucketer.toAdmissionYear("271234", BASE_YEAR, 1900))
                .isEqualTo(1927);
    }

    @DisplayName("6자리 숫자가 아니면 분류하지 않는다")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12345", "1234567", "22012a", "22-0123", " 220123"})
    void rejectsNonSixDigitStudentId(String studentId) {
        assertThat(AdmissionYearBucketer.toAdmissionYear(studentId, BASE_YEAR, MIN_YEAR)).isNull();
    }

    @DisplayName("유효 입학연도 범위를 벗어나면 분류하지 않는다")
    @Test
    void rejectsYearOutsideValidRange() {
        // 1989년은 하한(1990) 미만이다.
        assertThat(AdmissionYearBucketer.toAdmissionYear("891234", BASE_YEAR, MIN_YEAR)).isNull();
        // 미래 연도는 존재할 수 없다. (기준 2026, '27'은 1927로 해석되어 하한 미만)
        assertThat(AdmissionYearBucketer.toAdmissionYear("271234", BASE_YEAR, MIN_YEAR)).isNull();
    }

    @DisplayName("라벨은 학년이 아니라 '22학번' 형식으로 표기한다")
    @Test
    void labelUsesAdmissionYearNotGrade() {
        assertThat(AdmissionYearBucketer.toLabel(2022)).isEqualTo("22학번");
        assertThat(AdmissionYearBucketer.toLabel(1999)).isEqualTo("99학번");
        assertThat(AdmissionYearBucketer.toLabel(2005)).isEqualTo("05학번");
    }

    @DisplayName("네 자리 연도로 정렬하면 세기가 다른 학번의 순서가 뒤집히지 않는다")
    @Test
    void sortingByFourDigitYearKeepsOrder() {
        List<Integer> years = Stream.of("991234", "220123", "050123")
                .map(id -> AdmissionYearBucketer.toAdmissionYear(id, BASE_YEAR, MIN_YEAR))
                .sorted(Comparator.naturalOrder())
                .toList();

        // 두 자리 문자열('99','22','05')로 정렬했다면 05, 22, 99 순이 되어 1999가 마지막에 온다.
        assertThat(years).containsExactly(1999, 2005, 2022);
    }
}
