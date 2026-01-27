package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import java.util.List;
import lombok.Getter;

@Getter
public class ExcelParsingException extends CustomException {

    private final List<String> errorMessages;

    public ExcelParsingException(String message) {
        super(ErrorCode.INVALID_EXCEL_DATA, message);
        this.errorMessages = null;
    }

    public ExcelParsingException(String message, List<String> errorMessages) {
        super(ErrorCode.INVALID_EXCEL_DATA, message);
        this.errorMessages = errorMessages;
    }
}
