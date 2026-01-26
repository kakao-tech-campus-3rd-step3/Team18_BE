package com.kakaotech.team18.backend_server.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static LocalDateTime[] changeToDate(String recruitDate) {
        String[] recruitDates = recruitDate.replaceAll("\\s+", "").split("~");

        // 날짜 형식 지정 (yyyy-MM-dd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 문자열 → LocalDate 변환
        LocalDate startDate = LocalDate.parse(recruitDates[0], formatter);
        LocalDate endDate = LocalDate.parse(recruitDates[1], formatter);

        LocalDateTime recruitStart = startDate.atStartOfDay();
        LocalDateTime recruitEnd = endDate.atTime(23, 59, 59);

        return new LocalDateTime[]{recruitStart, recruitEnd};
    }

}
