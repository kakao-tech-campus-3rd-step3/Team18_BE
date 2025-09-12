package com.kakaotech.team18.backend_server.global.exception.exceptions;

public class ApplicationFormNotFoundException extends RuntimeException {
    public ApplicationFormNotFoundException(String message) {
        super(message);
    }
    public ApplicationFormNotFoundException(Long clubId) {
        super("해당 클럽의 활성화된 폼을 찾을 수 없습니다. clubId : "+clubId);
    }
}
