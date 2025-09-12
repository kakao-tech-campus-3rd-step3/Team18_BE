package com.kakaotech.team18.backend_server.global.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역에서 사용될 비즈니스 예외 코드를 정의하는 Enum 클래스입니다.
 * <p>
 * <strong>ErrorCode 원칙</strong>
 * <ul>
 *     <li>'예측 가능한 비즈니스 예외'를 명시적으로 다루기 위해 사용됩니다.</li>
 * </ul>
 *
 * <strong>활용 워크플로우 (정의 → 발생 → 처리)</strong>
 * <ol>
 *     <li><b>정의</b>: 이 Enum에 새로운 에러(메시지, HttpStatus)를 정의합니다.</li>
 *     <li><b>발생</b>: 서비스 계층에서 비즈니스 규칙 위반 시 <code>throw new CustomException(ErrorCode.YOUR_ERROR, "상세 정보");</code>를 호출합니다.</li>
 *     <li><b>처리</b>: <code>GlobalExceptionHandler</code>가 예외를 감지하여, 정의된 HttpStatus와 메시지로 일관된 에러 응답(JSON)을 생성합니다.</li>
 * </ol>
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청
    INVALID_INPUT_VALUE("입력 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    USER_NOT_FOUND("해당 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    CLUB_NOT_FOUND("해당 동아리가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    APPLICATION_NOT_FOUND("해당 지원서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT: 리소스 충돌
    USER_ALREADY_EXISTS("이미 존재하는 유저입니다.", HttpStatus.CONFLICT),

    // 500 INTERNAL_SERVER_ERROR: 서버 내부 에러
    INTERNAL_SERVER_ERROR("서버 내부에 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
