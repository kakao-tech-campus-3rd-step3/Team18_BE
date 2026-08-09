package com.kakaotech.team18.backend_server.domain.statistics.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AdmissionYearBucketer - 학번 → 입학연도")
class AdmissionYearBucketerTest {

    private static final int BASE_YEAR = 2026;

    @Test
    @DisplayName("앞 2자리를 4자리 입학연도로 변환한다 (22 → 2022)")
    void twoDigitToFourDigit() {
        assertThat(AdmissionYearBucketer.toAdmissionYear("220001", BASE_YEAR)).isEqualTo(2022);
        assertThat(AdmissionYearBucketer.toAdmissionYear("260001", BASE_YEAR)).isEqualTo(2026);
    }

    @Test
    @DisplayName("기준 연도 뒤 두 자리보다 크면 이전 세기로 해석한다 (99 → 1999)")
    void previousCenturyWhenAboveBase() {
        assertThat(AdmissionYearBucketer.toAdmissionYear("990001", BASE_YEAR)).isEqualTo(1999);
        assertThat(AdmissionYearBucketer.toAdmissionYear("000001", BASE_YEAR)).isEqualTo(2000);
    }

    @Test
    @DisplayName("오래된 연도도 미입력이 아니라 실제 연도를 반환한다 (묶음 판단은 호출부)")
    void oldYearsAreReturned() {
        assertThat(AdmissionYearBucketer.toAdmissionYear("100001", BASE_YEAR)).isEqualTo(2010);
        assertThat(AdmissionYearBucketer.toAdmissionYear("050001", BASE_YEAR)).isEqualTo(2005);
    }

    @Test
    @DisplayName("6자리 숫자가 아니면 null (7자리 이상·비지원서 경로 학번 등)")
    void nonSixDigitIsNull() {
        assertThat(AdmissionYearBucketer.toAdmissionYear(null, BASE_YEAR)).isNull();
        assertThat(AdmissionYearBucketer.toAdmissionYear("", BASE_YEAR)).isNull();
        assertThat(AdmissionYearBucketer.toAdmissionYear("2201", BASE_YEAR)).isNull();      // 4자리
        assertThat(AdmissionYearBucketer.toAdmissionYear("2200011", BASE_YEAR)).isNull();   // 7자리
        assertThat(AdmissionYearBucketer.toAdmissionYear("22000a", BASE_YEAR)).isNull();    // 숫자 아님
    }

    @Test
    @DisplayName("라벨은 학년이 아닌 '22학번' 형식")
    void label() {
        assertThat(AdmissionYearBucketer.toLabel(2022)).isEqualTo("22학번");
        assertThat(AdmissionYearBucketer.toLabel(2009)).isEqualTo("09학번");
    }
}
