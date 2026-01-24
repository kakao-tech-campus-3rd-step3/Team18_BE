package com.kakaotech.team18.backend_server.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    /**
     * @param dateInfo ex) 2025-10-16 10:30-11:00,2025-10-16 11:00-11:30
     * @return -> Map<선택한 날자, List<선택한 시간>>
     */
    public static Map<LocalDate, List<LocalTime>> parseDateAndTimeSlots(String dateInfo) {
        if (dateInfo == null || dateInfo.isBlank()) return new HashMap<>();

        Map<LocalDate, List<LocalTime>> result = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        for (String part : dateInfo.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            String[] dateAndTime = trimmed.split("\\s+", 2);
            if (dateAndTime.length != 2) {
                log.warn("인터뷰 선호 시간 형식이 올바르지 않음: {}", trimmed);
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(dateAndTime[0].trim(), dateFormatter);

                String timeRange = dateAndTime[1].trim();      // "10:30-11:00"
                String[] times = timeRange.split("-", 2);
                if (times.length != 2) {
                    log.warn("시간 범위 형식이 올바르지 않음: {}", trimmed);
                    continue;
                }

                LocalTime start = LocalTime.parse(times[0].trim(), timeFormatter);
                result.computeIfAbsent(date, k -> new ArrayList<>()).add(start);

            } catch (Exception e) {
                log.warn("인터뷰 선호 시간 파싱 실패: {}, error={}", trimmed, e.getMessage());
            }
        }
        return result;
    }
}
