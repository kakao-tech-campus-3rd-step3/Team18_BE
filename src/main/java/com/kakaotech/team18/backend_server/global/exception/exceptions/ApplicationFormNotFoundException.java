package com.kakaotech.team18.backend_server.global.exception.exceptions;

import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;

public class ApplicationFormNotFoundException extends CustomException {
    public ApplicationFormNotFoundException(Long clubId) {
        super(ErrorCode.FORM_NOT_FOUND,"해당 클럽의 활성화된 폼을 찾을 수 없습니다. clubId : "+clubId);
    }
}
