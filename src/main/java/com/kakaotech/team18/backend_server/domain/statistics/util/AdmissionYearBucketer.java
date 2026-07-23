package com.kakaotech.team18.backend_server.domain.statistics.util;

import java.util.regex.Pattern;

/**
 * 6자리 학번에서 입학연도를 뽑아냅니다.
 * <p>
 * <strong>이 클래스의 출력에는 학번 원본이 절대 포함되지 않는다.</strong> 앞 2자리에서 유도한 연도만 반환한다.
 */
public final class AdmissionYearBucketer {

    /** 지원서 접수 단계에서 학번은 6자리 숫자로 검증되지만, 다른 경로로 만들어진 User가 있을 수 있어 다시 확인한다. */
    private static final Pattern STUDENT_ID = Pattern.compile("\\d{6}");

    private AdmissionYearBucketer() {
    }

    /**
     * 학번을 4자리 입학연도로 변환합니다.
     * <p>
     * 두 자리 연도의 세기는 기준 연도로 판단한다. 기준 연도의 뒤 두 자리보다 크면 현재 세기에 존재할 수 없는
     * 연도이므로 이전 세기로 해석한다. (기준 2026년 → {@code 22} → 2022, {@code 99} → 1999)
     *
     * @param studentId 학번
     * @param baseYear  기준 연도 (보통 오늘 날짜의 연도)
     * @param minYear   유효한 입학연도의 하한
     * @return 4자리 입학연도. 6자리 숫자가 아니거나 유효 범위를 벗어나면 null
     */
    public static Integer toAdmissionYear(String studentId, int baseYear, int minYear) {
        if (studentId == null || !STUDENT_ID.matcher(studentId).matches()) {
            return null;
        }

        int twoDigit = Integer.parseInt(studentId.substring(0, 2));
        int baseTwoDigit = baseYear % 100;
        int century = (baseYear / 100) * 100;

        int year = (twoDigit <= baseTwoDigit) ? century + twoDigit : century - 100 + twoDigit;

        if (year < minYear || year > baseYear) {
            return null;
        }
        return year;
    }

    /**
     * 입학연도를 화면 표시용 라벨로 변환합니다.
     * <p>
     * 학년이 아니라 학번으로 표기한다. 휴학·편입 때문에 입학연도와 학년은 일치하지 않는다.
     *
     * @param year 4자리 입학연도
     * @return {@code 22학번} 형식의 라벨
     */
    public static String toLabel(int year) {
        return String.format("%02d학번", year % 100);
    }
}
