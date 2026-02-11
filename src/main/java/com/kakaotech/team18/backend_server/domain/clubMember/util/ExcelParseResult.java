package com.kakaotech.team18.backend_server.domain.clubMember.util;

import com.kakaotech.team18.backend_server.domain.clubMember.dto.ClubMemberSaveRequestDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ExcelParseResult {
    private final List<ClubMemberSaveRequestDto> successList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    public void addSuccess(ClubMemberSaveRequestDto dto) {
        successList.add(dto);
    }

    public void addError(String message) {
        errorMessages.add(message);
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}
