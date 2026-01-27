package com.kakaotech.team18.backend_server.domain.clubMember.util;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.AcademicStatus;
import com.kakaotech.team18.backend_server.domain.clubMember.entity.Role;
import com.kakaotech.team18.backend_server.global.exception.exceptions.ExcelParsingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class ExcelUtils {

    private static final List<String> HEADER_NAMES = Arrays.asList(
            "이름", "학번", "전화번호", "단과대학", "학과", "학적상태", "직책", "가입일자"
    );
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{3}-\\d{3,4}-\\d{4}$");
    private static final Pattern JOIN_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");

    private ExcelUtils() {
        // Utility class
    }

    public static ExcelParseResult parseExcel(MultipartFile file) throws IOException {
        ExcelParseResult result = new ExcelParseResult();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();

            // 1. 헤더 검증 (첫 번째 행)
            Row headerRow = sheet.getRow(firstRow);
            if (headerRow == null || !isValidHeader(headerRow)) {
                // 헤더가 틀리면 더 이상 진행할 수 없으므로 즉시 예외 발생 (파일 레벨 에러 취급)
                throw new ExcelParsingException("엑셀 헤더 양식이 올바르지 않습니다. 다음 순서로 작성해주세요: " + HEADER_NAMES);
            }

            DataFormatter formatter = new DataFormatter();

            // 2. 데이터 파싱 (두 번째 행부터)
            for (int i = firstRow + 1; i <= lastRow; i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isRowEmpty(dataRow, formatter)) {
                    continue; // 빈 행은 건너뛰기
                }

                try {
                    ClubMemberSaveRequestDto dto = parseRowToDto(dataRow, formatter, i + 1);
                    result.addSuccess(dto);
                } catch (IllegalArgumentException e) {
                    result.addError(String.format("%d행: %s", i + 1, e.getMessage()));
                }
            }
        }

        return result;
    }

    private static boolean isValidHeader(Row headerRow) {
        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < HEADER_NAMES.size(); i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = formatter.formatCellValue(cell).trim();
            if (!HEADER_NAMES.get(i).equals(cellValue)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static ClubMemberSaveRequestDto parseRowToDto(Row row, DataFormatter formatter, int rowNum) {
        String name = getCellValue(row, 0, formatter);
        String studentId = getCellValue(row, 1, formatter);
        String phoneNumber = getCellValue(row, 2, formatter);
        String college = getCellValue(row, 3, formatter);
        String department = getCellValue(row, 4, formatter);
        String academicStatusStr = getCellValue(row, 5, formatter);
        String roleStr = getCellValue(row, 6, formatter);
        String joinDateStr = getCellValue(row, 7, formatter);

        // 필수값 검증
        if (name.isEmpty()) throw new IllegalArgumentException("이름이 비어있습니다.");
        if (studentId.isEmpty()) throw new IllegalArgumentException("학번이 비어있습니다.");
        if (phoneNumber.isEmpty()) throw new IllegalArgumentException("전화번호가 비어있습니다.");
        if (college.isEmpty()) throw new IllegalArgumentException("단과대학이 비어있습니다.");
        if (department.isEmpty()) throw new IllegalArgumentException("학과가 비어있습니다.");
        if (academicStatusStr.isEmpty()) throw new IllegalArgumentException("학적상태가 비어있습니다.");
        if (roleStr.isEmpty()) throw new IllegalArgumentException("직책이 비어있습니다.");
        if (joinDateStr.isEmpty()) throw new IllegalArgumentException("가입일자가 비어있습니다.");

        // 형식 검증
        if (!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (010-XXXX-XXXX)");
        }
        if (!JOIN_DATE_PATTERN.matcher(joinDateStr).matches()) {
            throw new IllegalArgumentException("가입일자 형식이 올바르지 않습니다. (YYYY-MM)");
        }

        AcademicStatus academicStatus = parseAcademicStatus(academicStatusStr);
        Role role = parseRole(roleStr);

        return new ClubMemberSaveRequestDto(
                name, studentId, phoneNumber, college, department, academicStatus, role, joinDateStr
        );
    }

    private static String getCellValue(Row row, int cellNum, DataFormatter formatter) {
        Cell cell = row.getCell(cellNum);
        return (cell == null) ? "" : formatter.formatCellValue(cell).trim();
    }

    private static AcademicStatus parseAcademicStatus(String value) {
        return switch (value.trim()) {
            case "재학" -> AcademicStatus.ENROLLED;
            case "휴학" -> AcademicStatus.LEAVE_OF_ABSENCE;
            case "졸업" -> AcademicStatus.GRADUATED;
            case "수료" -> AcademicStatus.COMPLETED;
            case "제적" -> AcademicStatus.EXPELLED;
            default -> throw new IllegalArgumentException(
                    "학적상태는 '재학', '휴학', '졸업', '수료', '제적' 중 하나여야 합니다.");
        };
    }

    private static Role parseRole(String value) {
        return switch (value.trim()) {
            case "일반부원" -> Role.CLUB_MEMBER;
            case "운영진" -> Role.CLUB_EXECUTIVE;
            case "회장" -> Role.CLUB_ADMIN;
            default -> throw new IllegalArgumentException("직책은 '일반부원', '운영진', '회장' 중 하나여야 합니다.");
        };
    }
}
